document.getElementById('loginForm').addEventListener('submit', function(event) {
    event.preventDefault(); // Запобігаємо стандартній поведінці форми

    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;

    const data = {
        email: email,
        password: password
    };

    fetch('/api/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
        .then(response => {
            if (response.ok) {
                // Якщо статус 200 OK
                return response.json();
            } else if (response.status === 401) {
                // Якщо статус 401 Unauthorized
                throw new Error('Invalid email or password');
            } else {
                throw new Error('An unexpected error occurred');
            }
        })
        .then(data => {
            if (data.success) {
                window.location.href = '/books.html'; // Перенаправлення після успішного входу
            }
        })
        .catch(error => {
            document.getElementById('responseMessage').innerText = error.message;
        });

});
