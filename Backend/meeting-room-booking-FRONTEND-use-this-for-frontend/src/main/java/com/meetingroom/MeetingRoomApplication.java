//package com.meetingroom; // replace with your actual package name
//
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//
//@SpringBootApplication
//public class MeetingRoomApplication 
//{
//	public static void main(String[] args) 
//	{
//        System.out.println("🚀 Starting Task Manager Application...");
//
//        SpringApplication.run(MeetingRoomApplication.class, args);
//
//        System.out.println("✅ Meeting Room Application is now running at http://localhost:8080/");
//        System.out.print("I am yash");
//    }
//}

package com.meetingroom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MeetingRoomApplication {
    public static void main(String[] args) {
    	System.out.println("🚀 Starting Task Manager Application...");
        SpringApplication.run(MeetingRoomApplication.class, args);
        System.out.print("I am yash");
    }
}
