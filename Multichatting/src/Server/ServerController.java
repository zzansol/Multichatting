package Server;

import Controller.UserController;
import model.FriendRoom;
import model.Room;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Vector;



public class ServerController extends Thread{

    //Chatting.Service == 접속 클라이언트 한명!!


    Room myRoom;//클라이언트가 입장한 대화방

    FriendRoom friendRoom;


    //소켓관련 입출력서비스

    BufferedReader in;

    OutputStream out;



    Vector<ServerController> allV;//모든 사용자(대기실사용자 + 대화방사용자)

    Vector<ServerController> waitV;//대기실 사용자

    Vector<Room> roomV;//개설된 대화방 model.Room-vs(Vector) : 대화방사용자

    Vector<FriendRoom> friendRoomV;


    Socket s;



    String nickName;
    int userId;

    UserController userController =new UserController();


    public ServerController(Socket s, Server server) {

        allV=server.allV;

        waitV=server.waitV;

        roomV=server.roomV;

        friendRoomV=server.friendRoomV;



        this.s = s;



        try {

            in = new BufferedReader(new InputStreamReader(s.getInputStream()));

            out = s.getOutputStream();



            start();

        } catch (IOException e) {

            e.printStackTrace();

        }

    }//생성자



    @Override

    public void run() {

        try {



            while(true){

                String msg = in.readLine();//클라이언트의 모든 메시지를 받기



                if(msg == null) return; //비정상적인 종료



                if(msg.trim().length() > 0){

                    System.out.println("from Client: "+ msg +":"+

                            s.getInetAddress().getHostAddress());

                    //서버에서 상황을 모니터!!



                    String[] msgs =msg.split("\\|");

                    String protocol = msgs[0];



                    switch(protocol){

                        case "100": //대기실 접속


                            userId= Integer.parseInt(msgs[1]);


                            allV.add(this);//전체사용자에 등록

                            waitV.add(this);//대기실사용자에 등록








                            break;



                        case "150": //대화명 입력

                            nickName=msgs[1];



                            //최초 대화명 입력했을때 대기실의 정보를 출력

                            messageWait("160|"+ getRoomInfo());

                            messageWait("180|"+ getWaitInwon());


                            break;
                        case "151": //대화명 입력
                            nickName=msgs[1];

                            break;

                        case "160": //방만들기 (대화방 입장)

                            myRoom = new Room();

                            myRoom.title =msgs[1];//방제목

                            myRoom.count = 1;

                            myRoom.boss = nickName;



                            roomV.add(myRoom);



                            //대기실----> 대화방 이동!!

                            waitV.remove(this);


                            myRoom.userV.add(this);



                            messageRoom("200|"+nickName);//방인원에게 입장 알림



                            //대기실 사용자들에게 방정보를 출력

                            //예) 대화방명:JavaLove

                            //-----> roomInfo(JList) :  JavaLove--1

                            messageWait("160|"+ getRoomInfo());

                            messageWait("180|"+ getWaitInwon());

                            break;
                        case "171":

                            friendRoom = new FriendRoom();

                            friendRoom.title =msgs[1];//방제목

                            friendRoom.count = 1;

                            friendRoom.boss = nickName;



                            friendRoomV.add(friendRoom);



                            //대기실----> 대화방 이동!!







                            //대기실 사용자들에게 방정보를 출력

                            //예) 대화방명:JavaLove

                            //-----> roomInfo(JList) :  JavaLove--1






                        case "170": //(대기실에서) 대화방 인원정보

                            messageTo("170|"+getRoomInwon(msgs[1]));

                            break;



                        case "200": //방들어가기 (대화방 입장) ----> msgs[] = {"200","자바방"}

                            for (Room r : roomV) {//방이름 찾기!!

                                if (r.title.equals(msgs[1])) {//일치하는 방 찾음!!

                                    myRoom = r;

                                    myRoom.count++;//인원수 1증가

                                    break;

                                }

                            }//for

                            //대기실----> 대화방 이동!!

                                waitV.remove(this);
                                myRoom.userV.add(this);
                                messageRoom("200|"+nickName);//방인원에게 입장 알림



                                //들어갈 방의 title전달

                                messageTo("202|"+ myRoom.title);



                                messageWait("160|"+ getRoomInfo());

                                messageWait("180|"+ getWaitInwon());





                            break;
                        case "203":

                            for (FriendRoom r : friendRoomV) {//방이름 찾기!!
                                if (r.title.equals(msgs[1])) {//일치하는 방 찾음!!

                                    friendRoom = r;

                                    friendRoom.count++;//인원수 1증가

                                    break;

                                }

                            }//for


                            friendRoom.userV.add(this);
                            friendRoom("203|"+nickName);
                            messageTo("202|"+ friendRoom.title);

                            break;
                            /*
                        case "205":
                            userId= msgs[1];
                            System.out.println(userId);
                            int id=userDao.getUserPK(userId);
                            messageWait("205|"+ getFriend(id));
                            break;

*/
                        case "201":
                            messageRoom("201"+nickName);
                            break;
                        case "300": //메시지
                            messageRoom("300|["+nickName +"]▶ "+msgs[1]);
                            //클라이언트에게 메시지 보내기
                            break;
                        case "301": //메시지
                            friendRoom("301|["+nickName +"]▶ "+msgs[1]);
                            //클라이언트에게 메시지 보내기
                            break;



                        case "400": //대화방 퇴장

                            myRoom.count--;//인원수 감소



                            messageRoom("400|"+nickName);//방인원들에게 퇴장 알림!!



                            //대화방----> 대기실 이동!!

                            myRoom.userV.remove(this);

                            waitV.add(this);



                            //대화방 퇴장후 방인원 다시출력

                            messageRoom("175|"+getRoomInwon());



                            //대기실에 방정보 다시출력

                            messageWait("160|"+ getRoomInfo());



                            break;
                        case "401": //대화방 퇴장

                            friendRoom.count--;//인원수 감소



                            friendRoom("401|"+nickName);//방인원들에게 퇴장 알림!!



                            //대화방----> 대기실 이동!!

                            friendRoom.userV.remove(this);

                            waitV.add(this);



                            //대화방 퇴장후 방인원 다시출력


                            //대기실에 방정보 다시출력

                            break;








                    }//서버 switch

                }//if

            }//while

        }catch (SocketException se){
            System.out.println("["+nickName+"]님 접속 해제 되었습니다.");
        }
        catch (IOException e) {

            System.out.println("★");
            System.out.println("["+nickName+"]님 접속 해제 되었습니다.");
            e.printStackTrace();

        }

    }//run




    public String getRoomInfo(){


        String str="";

        for(int i=0; i<roomV.size(); i++){

            //"자바방--1,오라클방--1,JDBC방--1"

            Room r= roomV.get(i);

            str += r.title+"--"+r.count;

            if(i<roomV.size()-1)str += ",";

        }

        return str;

    }//getRoomInfo




    public String getRoomInwon(){//같은방의 인원정보

        String str="";

        for(int i=0; i<myRoom.userV.size(); i++){

            //"길동,라임,주원"

            ServerController ser= myRoom.userV.get(i);

            str += ser.nickName;

            if(i<myRoom.userV.size()-1)str += ",";

        }

        return str;

    }//getRoomInwon



    public String getRoomInwon(String title){//방제목 클릭시 방의 인원정보

        String str="";



        for(int i=0; i<roomV.size(); i++){

            //"길동,라임,주원"

            Room room = roomV.get(i);

            if(room.title.equals(title)){

                for(int j=0; j<room.userV.size(); j++){

                    ServerController ser= room.userV.get(j);

                    str += ser.nickName;

                    if(j<room.userV.size()-1)str += ",";

                }

                break;

            }

        }

        return str;

    }//getRoomInwon



    public String getWaitInwon(){

        String str="";

        for(int i=0; i<waitV.size(); i++){

            //"길동,라임,주원"

            ServerController ser= waitV.get(i);

            str += ser.nickName;

            if(i<waitV.size()-1)str += ",";

        }

        return str;


    }

    //getWaitInwon
    /*
    UserDao userDao=new UserDao();
    public String getFriend(int id){
        String str="";
        Vector<Controller> friendV = userDao.getFriend(id);
        for(int i=0; i<friendV.size(); i++){

            //"길동,라임,주원"

            Controller ser= friendV.get(i);

            str += ser.n;

            if(i<friendV.size()-1)str += ",";

        }

        System.out.println(str);
        return str;
    }
*/


    public void messageAll(String msg){//전체사용자

        //접속된 모든 클라이언트(대기실+대화방)에게 메시지 전달

        for(int i=0; i<allV.size(); i++){//벡터 인덱스

            ServerController serverController = allV.get(i); //각각의 클라이언트 얻어오기

            try {

                serverController.messageTo(msg);

            } catch (IOException e) {

                //에러발생 ---> 클라이언트 접속 끊음!!

                allV.remove(i--); //접속 끊긴 클라이언트를 벡터에서 삭제!!

                System.out.println("클라이언트 접속 끊음!!");

            }

        }

    }//messageAll



    public void messageWait(String msg){//대기실 사용자

        for(int i=0; i<waitV.size(); i++){//벡터 인덱스

            ServerController serverController = waitV.get(i); //각각의 클라이언트 얻어오기

            try {

                serverController.messageTo(msg);

            } catch (IOException e) {

                //에러발생 ---> 클라이언트 접속 끊음!!

                waitV.remove(i--); //접속 끊긴 클라이언트를 벡터에서 삭제!!

                System.out.println("클라이언트 접속 끊음!!");

            }

        }

    }//messageWait



    public void messageRoom(String msg){//대화방사용자

        for(int i=0; i< myRoom.userV.size(); i++){//벡터 인덱스

            ServerController serverController = myRoom.userV.get(i); //각각의 클라이언트 얻어오기

            try {

                serverController.messageTo(msg);

            } catch (IOException e) {

                //에러발생 ---> 클라이언트 접속 끊음!!

                myRoom.userV.remove(i--); //접속 끊긴 클라이언트를 벡터에서 삭제!!

                System.out.println("클라이언트 접속 끊음!!");

            }

        }

    }//messageAll

    public void friendRoom(String msg){

        for(int i=0; i< friendRoom.userV.size(); i++){//벡터 인덱스

            ServerController serverController = friendRoom.userV.get(i); //각각의 클라이언트 얻어오기

            try {

                serverController.messageTo(msg);

            } catch (IOException e) {

                //에러발생 ---> 클라이언트 접속 끊음!!

                friendRoom.userV.remove(i--); //접속 끊긴 클라이언트를 벡터에서 삭제!!

                System.out.println("클라이언트 접속 끊음!!");

            }

        }
    }



    public void messageTo(String msg) throws IOException{

        //특정 클라이언트에게 메시지 전달 (실제 서버--->클라이언트 메시지 전달)

        out.write(  (msg+"\n").getBytes()   );

    }



}