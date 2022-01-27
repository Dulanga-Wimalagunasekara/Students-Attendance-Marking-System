package db;

import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;

import java.sql.Connection;

public class DBConnection {
    private static DBConnection dbConnection;
    private Connection connection;

    private DBConnection(){

    }

    public static DBConnection getInstance(){
        return dbConnection==null? (dbConnection=new DBConnection()):dbConnection;

    }

    public void init(Connection connection){
        if (this.connection==null){
            this.connection=connection;
        }else if (this.connection!=connection){
            System.out.println("The connection is already initialized...!");
        }
    }

    public Connection getConnection(){
        if (connection==null){
            throw new RuntimeException("Initialize the connection First");
        }
        return this.connection;
    }
}
