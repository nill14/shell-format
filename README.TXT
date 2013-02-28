===========================================================================================
Shell-format for Java
===========================================================================================

A Java formatting tool suitable for parsing complex strings. 
Developed for parsing HQL, suitable for multiline, nested variables. 
Example: Hello, ${name}! 

See also standard Java formatters String#format(pattern, args) and MessageFormat#format(pattern, args)

===========================================================================================
Maven:
===========================================================================================

  	<dependency>
  		<groupId>me.format</groupId>
  		<artifactId>java-shell-format</artifactId>
  		<version>1.0.0</version>
  	</dependency>
  	
  	<!-- use SPI for SLF4J according to your choice -->
		<dependency>
			<groupId>ch.qos.logback</groupId>
			<artifactId>logback-classic</artifactId>
			<version>1.0.9</version>
		</dependency>
		<dependency>
			<groupId>ch.qos.logback</groupId>
			<artifactId>logback-core</artifactId>
			<version>1.0.9</version>
		</dependency>  
		
===========================================================================================
Simple snippet:
===========================================================================================

		ShellFormat shellFormat = ShellFormat.dictionary(
				"pom",		"World${exm}",
				"exm", 		"!!!"
		).compile();

		String result = shellFormat.format("Hello $pom");
		System.out.println(result); //Hello World!!!
		
===========================================================================================