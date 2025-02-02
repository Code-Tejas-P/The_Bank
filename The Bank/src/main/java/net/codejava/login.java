package net.codejava;

import DataBase.constants;

import java.util.Scanner;

public class login {
    constants c= new constants();
    Scanner scan=new Scanner(System.in);
    public boolean login(){
        System.out.println("Enter Username ");
        String u_name=scan.next();
        System.out.println("Enter Password");
        String pass = scan.next();
        if(c.validateUser(u_name,pass)) {
            System.out.println("Login successful!");
            return true;
        }
        else {
            System.out.println("Incorrect username or password.");
        }

        return false;
    }
    public boolean new_user(){

        System.out.println("Enter your Name ");
        String name = scan.next();
        System.out.println("Enter Username ");
        String u_name = scan.next();
        System.out.println("Enter Password");
        String pass = scan.next();
        c.insert_user(name, u_name, pass);
        return false;
    }
}

