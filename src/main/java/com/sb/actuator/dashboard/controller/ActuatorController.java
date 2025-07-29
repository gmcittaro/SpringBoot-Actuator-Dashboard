package com.sb.actuator.dashboard.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/actuator")
public class ActuatorController {

	@GetMapping("/dashboard")
	public String getActuatorDashboard() {
		return "actuator-dashboard";
	}
	
}
