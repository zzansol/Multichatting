package Server;

import Controller.ChatController;
import model.FriendRoom;
import model.GetRoomRes;
import model.Room;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;



public class Server implements Runnable{



    //Server클래스: 소켓을 통한 접속서비스, 접속클라이언트 관리



    Vector<ServerController> allV;//모든 사용자(대기실사용자 + 대화방사용자)

    Vector<ServerController> waitV;//대기실 사용자

    Vector<Room> roomV;//개설된 대화방 model.Room-vs(Vector) : 대화방사용자
    Vector<FriendRoom> friendRoomV;


    ChatController chatController =new ChatController();
    Room myRoom;
    FriendRoom friendRoom;


    public Server() {

        allV = new Vector<>();

        waitV = new Vector<>();

        roomV = new Vector<>();

        friendRoomV=new Vector<>();

        Vector<GetRoomRes> friendArr;
        friendArr=chatController.getFriendRoomList();
        for(GetRoomRes getRoomRes : friendArr){
            friendRoom= new FriendRoom();
            friendRoom.title= getRoomRes.getTitle();
            friendRoom.count= getRoomRes.getCount();
            friendRoom.boss= getRoomRes.getBoss();
            friendRoomV.add(friendRoom);
        }

        Vector<GetRoomRes> arr;
        arr= chatController.getRoomList();
        for (GetRoomRes getRoomRes :arr){
            myRoom=new Room();
            myRoom.title= getRoomRes.getTitle();
            myRoom.count= getRoomRes.getCount();
            myRoom.boss= getRoomRes.getBoss();
            roomV.add(myRoom);
        }



        //Thread t = new Thread(run메소드의 위치);  t.start();

        new Thread(this).start();

    }//생성자





    @Override

    public void run(){

        try {

            ServerSocket ss = new ServerSocket(5000);

            //현재 실행중인 ip + 명시된 port ----> 소켓서비스



            System.out.println("객패개패팀 채팅 서버 시작");

            while(true){

                Socket s = ss.accept();//클라이언트 접속 대기

                //s: 접속한 클라이언트의 소켓정보

                ServerController ser = new ServerController(s, this);

                //allV.add(ser);//전체사용자에 등록

                //waitV.add(ser);//대기실사용자에 등록

            }



        } catch(IOException se){
            se.printStackTrace();
        }

    }//run



    public static void main(String[] args) {

        new Server();

    }





}