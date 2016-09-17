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

