console.log("script.js 加载成功");

const usernameInput = document.getElementById("username-input");
const passwordInput = document.getElementById("password-input");
const createUserButton = document.getElementById("create-user-button");
const messageArea = document.getElementById("message-area");

createUserButton.addEventListener("click", function (event) {
    event.preventDefault();
    if (!usernameInput.value || !passwordInput.value) {
        messageArea.textContent = '用户名和密码不能为空！';
        messageArea.style.color = 'red';
        return;
    }
    let userToSend = {
        username: usernameInput.value,
        passwordHash: passwordInput.value // Send raw password
    };
    fetch("user/registerByAdmin", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(userToSend)
    }).then(response => response.text()).then(data => {
        messageArea.innerText = data;
        messageArea.style.color = 'green';
    }).catch(error => {
        console.error('请求发生错误:', error);
        messageArea.textContent = '请求失败，请检查网络或后台服务。';
        messageArea.style.color = 'red';
    });
});