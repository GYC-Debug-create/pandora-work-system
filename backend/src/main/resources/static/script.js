console.log("script.js 加载成功");
const usernameInput=document.getElementById("username-input")
const passwordInput=document.getElementById("password-input")
const registerButton=document.getElementById("register-button")
const loginButton=document.getElementById("login-button")
const messageArea=document.getElementById("message-area")
registerButton.addEventListener("click",function (event){
    event.preventDefault();
    if (!usernameInput.value || !passwordInput.value) {
        messageArea.textContent = '用户名和密码不能为空！';
        messageArea.style.color = 'red';
        return; // 提前结束，不发送请求
    }
    let userToSend={
        username:usernameInput.value,
        password:passwordInput.value
    }
    fetch("user/register",{
        method:"POST",
        headers:{
            "Content-Type":"application/json"

        },
        body:JSON.stringify(userToSend)
    }).then(response=>response.text()).then(data=>{
        messageArea.innerText=data
    }).catch(error=>{
        console.error('请求发生错误:', error);
        messageArea.textContent = '请求失败，请检查网络或后台服务。';
        messageArea.style.color = 'red';
    })
})
loginButton.addEventListener("click",function (event){
    event.preventDefault();
    let userInStr="";

    if (!usernameInput.value || !passwordInput.value) {
        messageArea.textContent = '用户名和密码不能为空！';
        messageArea.style.color = 'red';
        return; // 提前结束，不发送请求
    }
    let userToSend={
        username:usernameInput.value,
        password:passwordInput.value
    }
    fetch("user/login",{
        method:"POST",
        headers: {
            "Content-Type": "application/json"
        },
        body:JSON.stringify(userToSend)

    }).then(response=>response.json()).then(data=>{
        messageArea.innerText="欢迎！"+data.username

    })

        .catch(error=>{
                console.error('请求发生错误:', error);
                messageArea.textContent = '请求失败，请检查网络或后台服务。';
                messageArea.style.color = 'red';
    })

})
