package com.pennyslotcasinobank.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.pennyslotscasinobank.ConnectionUtil;

public class HouseDAO {
	public Object getHouse(int id) {
		
		//connect to JDBC
		//statement in try will auto close connection
		try(Connection connection = ConnectionUtil.getConnection()) {
			
			System.out.println(connection);
			
			String sql = "SELECT id, build_name, apartment_number FROM houses WHERE id = ?";

			PreparedStatement statement = connection.prepareStatement(sql);
			
			statement.setString(1, "1");
			//statement.setString(2, "2");
			
			ResultSet result = statement.executeQuery(sql);
			if(result.next()) {
				String buildName = result.getString("build_name");
				String apartmentNumber = result.getString("apartment_number");
				//return new House(id, buildName, apartmentNumber);
			}
			
			//to handle multiple results returned:
			//while(result.next())

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
}
