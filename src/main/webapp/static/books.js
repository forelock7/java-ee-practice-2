function createButton(text, onClickFunction) {
    const button = document.createElement('button');
    button.type = 'button';
    button.innerText = text;
    button.onclick = onClickFunction;
    button.id = 'add-update-form-button'
    return button;
}

function setupAddButton() {
    const buttonContainer = document.getElementById('add-update-button-container');
    buttonContainer.innerHTML = ''; // Clear any existing content
    const addButton = createButton('Add', submitForm);
    buttonContainer.appendChild(addButton);
}

function setupUpdateButton() {
    const buttonContainer = document.getElementById('add-update-button-container');
    buttonContainer.innerHTML = ''; // Clear any existing content
    const updateButton = createButton('Update', submitForm);
    buttonContainer.appendChild(updateButton);
}

function submitForm() {
    const id = document.getElementById('book-id').value;  // Отримуємо ID, якщо воно вказано
    const title = document.getElementById('title').value;
    const author = document.getElementById('author').value;
    const genre = document.getElementById('genre').value;
    const year = Number.parseInt(document.getElementById('year').value);

    const data = {
        // id: id,
        title: title,
        author: author,
        genre: genre,
        year: year
    };
    if (id) {
        updateBook(id, data);
    } else {
        addBook(data);
    }
}

function addBook(data) {
    console.log('Adding book:', data);
    // Відправляємо дані на сервер для додавання завдання
    fetch('/api/books', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
        .then(response => response.json())
        .then(() => {
            resetAddUpdateForm();
            // Оновлюємо список книг на сторінці
            loadBooksTable();
        })
        .catch(error => console.error('Error:', error));
}

function updateBook(book_id, data) {
    console.log('Updating book:', book_id, data);
    // Відправляємо дані на сервер для додавання завдання
    fetch(`/api/books/${book_id}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Book not found');
            }
            resetAddUpdateForm();
            loadBooksTable();
        })
        .catch(error => console.error('Error:', error));
}

function loadUpdateForm(book_id) {
    console.log('Loading book data to update:', book_id);
    fetch(`/api/books/${book_id}`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        },
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Book not found');
            }
            return response.json();
        })
        .then(book => {
            document.getElementById('book-id').value = book.id;
            document.getElementById('title').value = book.title;
            document.getElementById('author').value = book.author;
            document.getElementById('genre').value = book.genre;
            document.getElementById('year').value = book.year;

            setupFormAuthor(false);
            setupUpdateButton(); // Change button to 'Update'
        })
        .catch(error => console.error('Error:', error));
}

function deleteBook(book_id) {
    fetch(`/api/books/${book_id}`, {
        method: 'DELETE',
    })
        .then(response => {
            if(response.ok) {
                // Оновлюємо список книг на сторінці
                loadBooksTable();
            } else {
                // Замість response.message використовуйте текст відповіді
                return response.text().then(text => {
                    throw new Error(text || 'Unknown error occurred');
                });
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Failed to delete book: ' + error.message);
        });
}

function loadBooksTable() {
    fetch('/api/books')
        .then(response => response.json())
        .then(books => {
            const booksList = document.getElementById('books-list');
            // Очищаємо список перед додаванням нових даних
            booksList.innerHTML = '';
            books.forEach(book => {
                const row = booksList.insertRow();
                row.innerHTML = `
                    <td>${book.id}</td>
                    <td>${book.title}</td>
                    <td>${book.author}</td>
                    <td>${book.genre}</td>
                    <td>${book.year}</td>
                    <td class="table-button-cell">
                        <button class="table-button delete-book-button" onclick="deleteBook(${book.id})">Delete</button>
                        <button class="table-button update-book-button" onclick="loadUpdateForm(${book.id})">Edit</button>
                    </td>
                `;
            });
        });
}

function resetAddUpdateForm() {
    setupFormAuthor();
    setupAddButton();
    // Очищаємо форму після додавання студента
    document.getElementById('book-form').reset();
    // Видаляємо значення ID для уникнення непередбачуваної поведінки
    document.getElementById('book-id').value = '';
    validateForm();
}

function setupFormAuthor(isAddBook = true) {
    document.getElementById('form-author').innerText = isAddBook ? "Add Book" : "Update Book";
}

function validateForm() {
    // Check if all required fields are filled
    const title = document.getElementById('title').value.trim();
    const author = document.getElementById('author').value.trim();
    const genre = document.getElementById('genre').value.trim();
    const year =  document.getElementById('year').value.trim();
    const isFormValid = title && author && genre && year; // Simple truthy check

    // Enable or disable the button based on the form validity
    document.getElementById('add-update-form-button').disabled = !isFormValid;
}

document.getElementById('book-form').addEventListener('input', validateForm);

// Initial setup
window.onload = function() {
    setupFormAuthor();
    setupAddButton();
    validateForm()
    loadBooksTable();
};

