# RemoteFileManager
<p>
Build a java based bridge to support Angular File Manager

Now able to run under <b>WINDOWS</b> environment, besides <b>LINUX</b>.
</p>
<h1>Requirement</h1>
1) JDK 1.7<br>
2) git<br>
3) Maven<br>
4) Internet<br>

<h1>Run</h1>

<h3>WINDOWS</h3>
1) git clone https://github.com/Walsemaj/RemoteFileManager<br>
2) call RemoteFileManager/remoteFS.bat<br>

<h3>LINUX</h3>
1) git clone https://github.com/Walsemaj/RemoteFileManager<br>
2) ./RemoteFileManager/remoteFS.sh start<br>

<p>
Go to http://localhost:8080/<br>
</p>
<hr>
<h3>More Features <i>in angular-filemanager.properties<i></h3>
1) Functions only be available for specific directory or file<br>
restricted.functions=download,rename,copy,move,remove,getContent,edit,changePermissions,compress,extract<br>
whitelisted.files=/test/test1234.txt,/test/test3456.txt<br>
OR<br>
whitelisted.files=/test/test1234.txt,\\<br>
/test/test3456.txt<br>
<br>
2) Home directory setup<br>
Root<br>
o repository.base.url=/<br>
Web Content Root<br>
o repository.base.url=/<br>
o context.get.real.path=true<br>
