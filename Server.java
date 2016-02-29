import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import java.lang.*;

import java.util.Random;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.gossip.protobuf.MessageProtos.hearGossipRequest;
import com.gossip.protobuf.MessageProtos.Clock;
	
public class Server implements GossipInterface {
	
	private int TotalProcesses;
	private int[] VectorClock;
	private int ProcessId;
	private int Count;
	private long WaitTime = 5000;

	public Server(int x, int y) throws RemoteException
	{
		TotalProcesses = x;
		VectorClock = new int[(x+1)];
		ProcessId = y;
	}

	public static int getRandom(int High){
		Random r = new Random();
		int Low = 1;
		High++;
		int Result = r.nextInt(High-Low) + Low;
		return Result;
	}

	public void hearGossip(byte[] arr)
	{
		try
		{
			hearGossipRequest msg = hearGossipRequest.parseFrom(arr);
			if(msg.getClk().getCount() > this.VectorClock[msg.getClk().getProcess()])
			{
				System.out.println("Accept " + msg.getMsg());
				this.VectorClock[msg.getClk().getProcess()] = msg.getClk().getCount();
				Thread.sleep(this.WaitTime);
				this.processGossip(msg.getMsg(),msg, 1);
			}
			else
			{
				System.out.println("Reject " + msg.getMsg());
			}
		}
		catch (Exception e)
		{
			System.err.println("Exception: " + e.toString());
			e.printStackTrace();
		}
	}

	public void processGossip(String msg, hearGossipRequest req, int Check)
	{
		if(Check == 0)
		{
			this.Count++;
			this.VectorClock[this.ProcessId] = this.Count;
			Clock clock = Clock.newBuilder().setProcess(this.ProcessId).setCount(this.Count).build();

			req = hearGossipRequest.newBuilder().setMsg(msg).setClk(clock).build();
		}

		int p1 = this.getRandom(this.TotalProcesses);
		while(p1 == req.getClk().getProcess()){
			p1 = this.getRandom(this.TotalProcesses);
		}
		int p2 = this.getRandom(this.TotalProcesses);
		while(p2 == req.getClk().getProcess()){
			p2 = this.getRandom(this.TotalProcesses);
		}

		try
		{
			Registry registry = LocateRegistry.getRegistry();
			GossipInterface stub1 = (GossipInterface) registry.lookup("Gossip".concat(Integer.toString(p1)));
			stub1.hearGossip(req.toByteArray());

			GossipInterface stub2 = (GossipInterface) registry.lookup("Gossip".concat(Integer.toString(p2)));
			stub2.hearGossip(req.toByteArray());
		}
		catch (Exception e) {
			System.err.println("Exception: " + e.toString());
			e.printStackTrace();
		}
	}

	public static void main(String args[])
	{
		long WaitProcess = 20000;
		try
		{
			Server obj = new Server(Integer.parseInt(args[1]),Integer.parseInt(args[0]));
			GossipInterface stub = (GossipInterface) UnicastRemoteObject.exportObject(obj, 0);
			
			Registry registry = LocateRegistry.getRegistry();
			registry.bind("Gossip".concat(args[0]), stub);
			Thread.sleep(WaitProcess);
			if(args.length == 4)
			{
				try(BufferedReader br = new BufferedReader(new FileReader(args[3])))
				{
					for(String line; (line = br.readLine()) != null; ) {
						obj.processGossip(line, null, 0);
						Thread.sleep(obj.WaitTime);
					}
				}
			}
		}
		catch (Exception e)
		{
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
		}
	}
}
