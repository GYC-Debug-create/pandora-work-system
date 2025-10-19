document.addEventListener('DOMContentLoaded', function() {
    const departmentSelect = document.getElementById('department-select');

    // 1. 页面加载时，获取部门列表并填充下拉框
    fetch('/api/departments')
        .then(response => response.json())
        .then(data => {
            data.forEach(department => {
                const option = document.createElement('option');
                option.value = department.departmentId;
                option.textContent = department.departmentName;
                departmentSelect.appendChild(option);
            });
        })
        .catch(error => console.error('获取部门列表失败:', error));

    const createUserButton = document.getElementById('create-user-button');
    const messageArea = document.getElementById('message-area');

    createUserButton.addEventListener('click', function (event) {
        event.preventDefault();

        const userToSend = {
            username: document.getElementById('username-input').value,
            passwordHash: document.getElementById('password-input').value,
            fullName: document.getElementById('fullname-input').value,
            email: document.getElementById('email-input').value,
            mbtiType: document.getElementById('mbti-input').value,
            departmentId: departmentSelect.value
        };

        if (!userToSend.username || !userToSend.passwordHash) {
            messageArea.textContent = '用户名和密码不能为空！';
            messageArea.style.color = 'red';
            return;
        }

        // 2. 发送包含所有字段的创建请求
        fetch("/user/registerByAdmin", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(userToSend)
        })
            .then(response => response.text())
            .then(data => {
                messageArea.innerText = data;
                messageArea.style.color = 'green';
                // 成功后可以清空表单
                document.getElementById('create-user-form').reset();
            })
            .catch(error => {
                console.error('请求发生错误:', error);
                messageArea.textContent = '请求失败，请检查网络或后台服务。';
                messageArea.style.color = 'red';
            });
    });
});