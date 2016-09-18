#include "ns3/core-module.h"
#include "ns3/network-module.h"
#include "ns3/csma-module.h"
#include "ns3/internet-module.h"
#include "ns3/point-to-point-module.h"
#include "ns3/netanim-module.h"
#include "ns3/applications-module.h"
#include "ns3/ipv4-global-routing-helper.h"

using namespace ns3;

NS_LOG_COMPONENT_DEFINE("ArpMITMAttackSimulation");

class ARPSpoofApp : public Application
{
public:
  ARPSpoofApp();
  virtual ~ARPSpoofApp();

  void Setup(Ptr<NetDevice> device,
                          Address sourceMAC,
                          Ipv4Address sourceIP,
                          Address targetMAC,
                          Ipv4Address targetIP,
                          uint32_t packetSize,
                          uint32_t nPackets,
                          DataRate dataRate);

private:
  virtual void StartApplication(void);
  virtual void StopApplication(void);

  void ScheduleTx(void);
  void SendPacket(void);

  uint32_t m_packetSize;
  uint32_t m_nPackets;
  DataRate m_dataRate;
  EventId m_sendEvent;
  bool m_running;
  uint32_t m_packetsSent;

  Ptr<NetDevice> m_device;
  Address m_sourceMAC, m_targetMAC;
  Ipv4Address m_sourceIP, m_targetIP;
};

ARPSpoofApp::ARPSpoofApp()
  :  m_packetSize(0),
    m_nPackets(0),
    m_dataRate(0),
    m_sendEvent(),
    m_running(false),
    m_packetsSent(0)
{
}

ARPSpoofApp::~ARPSpoofApp()
{
}

void ARPSpoofApp::Setup(Ptr<NetDevice> device,
                        Address sourceMAC,
                        Ipv4Address sourceIP,
                        Address targetMAC,
                        Ipv4Address targetIP,
                        uint32_t packetSize,
                        uint32_t nPackets,
                        DataRate dataRate)
{
  m_device = device;
  m_sourceMAC = sourceMAC;
  m_sourceIP = sourceIP;
  m_targetMAC = targetMAC;
  m_targetIP = targetIP;
  m_packetSize = packetSize;
  m_nPackets = nPackets;
  m_dataRate = dataRate;
}

void ARPSpoofApp::StartApplication(void)
{
  m_running = true;
  m_packetsSent = 0;
  SendPacket();
}

void ARPSpoofApp::StopApplication(void)
{
  m_running = false;

  if (m_sendEvent.IsRunning()) {
    Simulator::Cancel(m_sendEvent);
  }
}

void ARPSpoofApp::SendPacket(void)
{
  ArpHeader arpHeader;
  arpHeader.SetReply(m_sourceMAC, m_sourceIP, m_targetMAC, m_targetIP);

  Ptr<Packet> packet = Create<Packet>();
  packet->AddHeader(arpHeader);

  m_device->Send(packet, m_targetMAC, 0x0806);

  if (++m_packetsSent < m_nPackets) {
    ScheduleTx();
  }
}

void ARPSpoofApp::ScheduleTx(void)
{
  if (m_running) {
    Time tNext(Seconds(m_packetSize * 8/ static_cast<double> (m_dataRate.GetBitRate())));
    m_sendEvent = Simulator::Schedule(tNext, &ARPSpoofApp::SendPacket, this);
  }
}

int main(int argc, char *argv[])
{
  // Constants
  int numberOfNodes = 3;

  LogComponentEnable("UdpEchoServerApplication", LOG_LEVEL_INFO);
  LogComponentEnable("UdpEchoClientApplication", LOG_LEVEL_INFO);

  NodeContainer csmaNodes;
  csmaNodes.Create(numberOfNodes);

  CsmaHelper csma;
  csma.SetChannelAttribute("DataRate", StringValue("3Kbps"));
  csma.SetChannelAttribute("Delay", TimeValue(NanoSeconds(6560)));

  NetDeviceContainer csmaDevices;
  csmaDevices = csma.Install(csmaNodes);

  InternetStackHelper stack;
  stack.Install(csmaNodes);

  Ipv4AddressHelper address;
  address.SetBase("10.0.0.0", "255.255.255.0");
  Ipv4InterfaceContainer csmaInterfaces;
  csmaInterfaces = address.Assign(csmaDevices);

  UdpEchoServerHelper echoServer(9);

  ApplicationContainer serverApps = echoServer.Install(csmaNodes.Get(0));
  serverApps.Start(Seconds(1.0));
  serverApps.Stop(Seconds(10.0));

  UdpEchoClientHelper echoClient(csmaInterfaces.GetAddress(0), 9);
  echoClient.SetAttribute("MaxPackets", UintegerValue(1));
  echoClient.SetAttribute("Interval", TimeValue(Seconds(1.0)));
  echoClient.SetAttribute("PacketSize", UintegerValue(1024));

  ApplicationContainer clientApps = echoClient.Install(csmaNodes.Get(numberOfNodes - 1));
  clientApps.Start(Seconds(2.0));
  clientApps.Stop(Seconds(9.0));

  Ptr<ARPSpoofApp> app = CreateObject<ARPSpoofApp>();
  app->Setup(csmaDevices.Get(1), // Device through which to send
             csmaDevices.Get(1)->GetAddress(), // Source MAC
             csmaNodes.Get(1)->GetObject<Ipv4>()->GetAddress(1, 0).GetLocal(), // Source IP
             csmaDevices.Get(0)->GetAddress(), // Target MAC
             csmaNodes.Get(0)->GetObject<Ipv4>()->GetAddress(1, 0).GetLocal(), // Target IP
             1040, 1000, DataRate("1Mbps"));
  csmaNodes.Get(1)->AddApplication(app);
  app->SetStartTime(Seconds(2.0));
  app->SetStopTime(Seconds(6.0));

  Ipv4GlobalRoutingHelper::PopulateRoutingTables();
  
  csma.EnablePcap("arp", csmaDevices.Get(1), true);
  
  AnimationInterface::SetConstantPosition(csmaNodes.Get(0), 0, 0);
  AnimationInterface::SetConstantPosition(csmaNodes.Get(1), 50, 0);
  AnimationInterface::SetConstantPosition(csmaNodes.Get(2), 25, 50);
  AnimationInterface anim("arp.xml");
  anim.EnablePacketMetadata(true);

  Simulator::Run();
  Simulator::Destroy();

  return 0;
}

