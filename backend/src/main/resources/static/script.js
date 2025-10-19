const usernameInput = document.getElementById("username-input");
const passwordInput = document.getElementById("password-input");
const loginButton = document.getElementById("login-button");
const messageArea = document.getElementById("message-area");

loginButton.addEventListener("click", function (event) {
    event.preventDefault();
    if (!usernameInput.value || !passwordInput.value) {
        messageArea.textContent = '用户名和密码不能为空！';
        messageArea.style.color = 'red';
        return;
    }
    let userToSend = {
        username: usernameInput.value,
        passwordHash: passwordInput.value
    };

    fetch("/user/login", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify(userToSend)
    })
        .then(response => {
            // 关键修正：检查HTTP响应状态码
            if (response.ok) { // response.ok 为 true 代表状态码在 200-299 之间
                return response.json();
            } else {
                // 如果状态码不是2xx (例如401), 则将错误信息文本传递给下一个.catch
                return response.text().then(text => { throw new Error(text) });
            }
        })
        .then(data => {
            // 只有在登录成功时才会执行这里
            messageArea.textContent = "登录成功！欢迎, " + data.username + "！正在跳转...";
            messageArea.style.color = 'green';
            // 成功后跳转到管理员后台页面
            window.location.href = '/admin-dashboard.html';
        })
        .catch(error => {
            // 所有错误（网络错误、登录失败）都会在这里被捕获
            console.error('登录请求发生错误:', error);
            messageArea.textContent = error.message || '登录失败，请检查网络或后台服务。';
            messageArea.style.color = 'red';
        });
});