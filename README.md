# 🚀 SpringBoot Actuator Dashboard

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](http://makeapullrequest.com)

Transform your Spring Boot monitoring experience with a beautiful, secure dashboard that puts all your Actuator data at your fingertips! This project serves as a perfect starter template for developers who want to build comprehensive monitoring solutions.

## ✨ Why Choose This Dashboard?

Stop struggling with raw JSON endpoints and scattered monitoring data! This project delivers a stunning, unified dashboard that transforms Spring Boot Actuator's powerful monitoring capabilities into an intuitive, visually appealing interface that your team will actually want to use.

## 🎯 Key Features

- **🖥️ Beautiful Dashboard Interface** - Clean, responsive design that makes monitoring a pleasure
- **🔒 Enterprise-Grade Security** - IP-based access control to keep your sensitive data protected  
- **📊 Real-Time Monitoring** - Live updates of your application's health, metrics, and performance
- **⚡ Zero Configuration** - Drop it in and go! Minimal setup required
- **📱 Mobile Responsive** - Monitor your apps from anywhere, on any device
- **🎨 Modern UI/UX** - Built with modern web standards for the best user experience
- **🛠️ Developer-Friendly** - Perfect as a foundation for your custom monitoring solutions

## 🚀 Quick Start

### Prerequisites

- ☕ Java 17+
- 📦 Maven 3.6+
- ⚡ Your favorite IDE

### Installation

1. **Clone this repository**
   ```bash
   git clone https://github.com/yourusername/SpringBoot-Actuator-Dashboard.git
   cd SpringBoot-Actuator-Dashboard
   ```

2. **Configure allowed IPs (Optional)**
   ```properties
   # application.properties
   actuator.security.allowed-ips=127.0.0.1,192.168.1.0/24
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

4. **Access your dashboard**
   ```
   http://localhost:8080/actuator/dashboard
   ```

## 🎨 Dashboard Components

Your new monitoring dashboard includes:

- **📈 Health Status** - Instant overview of application health
- **💾 Memory Usage** - Real-time memory consumption tracking  
- **🔧 Environment Properties** - Easy access to configuration details
- **📊 Metrics Visualization** - Beautiful charts and graphs
- **🔍 Thread Dump Analysis** - Deep dive into application performance
- **📝 Loggers Management** - Dynamic log level configuration

## 🛡️ Advanced Security Features

### Enterprise-Grade IP-Based Access Control

Our security implementation uses Spring Security 6+ with a sophisticated dual-chain configuration that provides maximum protection for Actuator endpoints while keeping your application endpoints publicly accessible.

```java
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    
    @Bean
    @Order(1)
    public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
            .securityMatcher("/actuator/**")
            .authorizeHttpRequests(auth -> 
                auth.requestMatchers("/actuator/**").authenticated()
            )
            .addFilterBefore(ipValidationFilter, UsernamePasswordAuthenticationFilter.class)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .build();
    }
}
```

### Key Security Features

- **🔒 Dual Security Chains** - Separate security configurations for Actuator vs public endpoints
- **🌐 Advanced IP Filtering** - Custom IP validation filter with CIDR support
- **⚡ Stateless Authentication** - Optimized for performance with no session overhead
- **🛡️ Security Headers** - Complete OWASP-compliant header configuration
- **🔐 CSRF Protection** - Intelligent CSRF handling for different endpoint types

**Supported IP Formats:**
- Individual IPs: `192.168.1.100`
- IP ranges with CIDR: `192.168.1.0/24`  
- Multiple entries: `127.0.0.1,10.0.0.0/8,172.16.0.0/12`
- Private network ranges: `10.0.0.0/8`, `172.16.0.0/12`, `192.168.0.0/16`

## ⚙️ Configuration Options

| Property | Description | Default |
|----------|-------------|---------|
| `security.actuator.enabled` | Enable/disable IP filtering | `true` |
| `security.actuator.allowed-ips` | Comma-separated list of allowed IPs | `127.0.0.1` |

## 🏗️ Architecture Overview

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Web Browser   │───▶│  Security Filter │───▶│ Actuator APIs   │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                                │                        │
                                ▼                        ▼
                       ┌──────────────────┐    ┌─────────────────┐
                       │   Dashboard UI   │◀───│ Spring Boot App │
                       └──────────────────┘    └─────────────────┘
```

## 🤝 Contributing

We love contributions! Here's how you can help make this project even better:

1. **🍴 Fork** the repository
2. **🌿 Create** your feature branch (`git checkout -b feature/AmazingFeature`)
3. **💾 Commit** your changes (`git commit -m 'Add some AmazingFeature'`)
4. **📤 Push** to the branch (`git push origin feature/AmazingFeature`)
5. **🔄 Open** a Pull Request

### Ideas for Contributions

- 🎨 New dashboard themes and dark mode enhancements
- 📊 Additional metric visualizations and charts
- 🔧 Extended configuration options and customization
- 🌐 Internationalization (i18n) support
- 📚 Documentation improvements and tutorials
- 🔌 Custom widget development and plugin system
- 🛡️ Enhanced security features and authentication methods

## 📋 Roadmap

- [ ] 🔔 Alert system for critical metrics
- [ ] 💾 Historical data storage and analytics
- [ ] 📧 Email and Slack notifications
- [ ] 🐳 Docker containerization support
- [ ] ☁️ Cloud deployment guides (AWS, Azure, GCP)
- [ ] 🔌 Plugin system for custom widgets
- [ ] 📱 Mobile app companion

## 🛠️ Built With

- **[Spring Boot 3.5.4](https://spring.io/projects/spring-boot)** - Application framework
- **[Java 17+](https://www.oracle.com/java/)** - Programming language with modern features
- **[Spring Security 6+](https://spring.io/projects/spring-security)** - Advanced security with dual-chain configuration
- **[Thymeleaf](https://www.thymeleaf.org/)** - Template engine
- **[Project Lombok](https://projectlombok.org/)** - Code generation
- **[Spring Boot Actuator](https://spring.io/guides/gs/actuator-service/)** - Production monitoring

## 📄 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

This means you can:
- ✅ Use commercially
- ✅ Modify and distribute
- ✅ Use privately
- ✅ Sublicense

Just remember to include the original copyright notice!

## 🙏 Acknowledgments

- **Spring Boot team** for the amazing Actuator framework
- **The open-source community** for inspiration and feedback  
- **All contributors** who help make this project better every day

## 📞 Support & Community

- 📖 **Documentation**: Check our [Wiki](../../wiki)
- 🐛 **Bug Reports**: [Open an issue](../../issues)
- 💡 **Feature Requests**: [Start a discussion](../../discussions)
- 💬 **Questions**: Use [GitHub Discussions](../../discussions)

---

## ⭐ Show Your Support

If this project helped you, please consider:

- ⭐ **Starring** this repository
- 🍴 **Forking** it for your own projects  
- 📢 **Sharing** it with your network
- 🤝 **Contributing** to make it even better

**Ready to revolutionize your Spring Boot monitoring?**

[⭐ Star this repo](../../stargazers) • [🍴 Fork it](../../fork) • [📢 Share it](https://twitter.com/intent/tweet?text=Check%20out%20this%20amazing%20SpringBoot%20Actuator%20Dashboard!)

---

<div align="center">

**Made with ❤️ for the Spring Boot community**

[Report Bug](../../issues) · [Request Feature](../../issues) · [Contribute](CONTRIBUTING.md)

</div>
