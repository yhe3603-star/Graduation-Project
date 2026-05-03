CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) DEFAULT 'user',
    status VARCHAR(20) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS plants (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name_cn VARCHAR(100),
    name_dong VARCHAR(100),
    scientific_name VARCHAR(200),
    category VARCHAR(50),
    usage_way VARCHAR(100),
    habitat VARCHAR(500),
    efficacy TEXT,
    story TEXT,
    images TEXT,
    videos TEXT,
    documents TEXT,
    distribution VARCHAR(500),
    update_log TEXT,
    view_count INT DEFAULT 0,
    favorite_count INT DEFAULT 0,
    popularity INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS knowledge (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    type VARCHAR(50),
    therapy_category VARCHAR(100),
    disease_category VARCHAR(100),
    herb_category VARCHAR(100),
    content TEXT NOT NULL,
    formula TEXT,
    usage_method TEXT,
    steps TEXT,
    images TEXT,
    videos TEXT,
    documents TEXT,
    related_plants TEXT,
    update_log TEXT,
    popularity INT DEFAULT 0,
    view_count INT DEFAULT 0,
    favorite_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS inheritors (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    level VARCHAR(50),
    bio TEXT,
    specialties TEXT,
    experience_years INT,
    videos TEXT,
    images TEXT,
    documents TEXT,
    representative_cases TEXT,
    honors TEXT,
    update_log TEXT,
    view_count INT DEFAULT 0,
    favorite_count INT DEFAULT 0,
    popularity INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS qa (
    id INT AUTO_INCREMENT PRIMARY KEY,
    category VARCHAR(50),
    question VARCHAR(500) NOT NULL,
    answer TEXT,
    popularity INT DEFAULT 0,
    view_count INT DEFAULT 0,
    favorite_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS resources (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    category VARCHAR(50),
    files TEXT,
    description TEXT,
    update_log TEXT,
    view_count INT DEFAULT 0,
    download_count INT DEFAULT 0,
    favorite_count INT DEFAULT 0,
    popularity INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS comments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    username VARCHAR(50),
    target_type VARCHAR(50) NOT NULL,
    target_id INT NOT NULL,
    content TEXT NOT NULL,
    reply_to_id INT,
    reply_to_user_id INT,
    reply_to_username VARCHAR(50),
    likes INT DEFAULT 0,
    reply_count INT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS favorites (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    target_type VARCHAR(50) NOT NULL,
    target_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS feedback (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    user_name VARCHAR(50),
    type VARCHAR(50),
    title VARCHAR(200),
    content TEXT,
    contact VARCHAR(200),
    status VARCHAR(20) DEFAULT 'pending',
    reply TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS quiz_questions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    question VARCHAR(500) NOT NULL,
    options TEXT,
    answer VARCHAR(200),
    category VARCHAR(50),
    correct_answer VARCHAR(200),
    explanation TEXT
);

CREATE TABLE IF NOT EXISTS quiz_record (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    score INT DEFAULT 0,
    total_questions INT DEFAULT 0,
    correct_answers INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS plant_game_record (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    difficulty VARCHAR(20),
    score INT DEFAULT 0,
    correct_count INT DEFAULT 0,
    total_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS operation_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    username VARCHAR(50),
    module VARCHAR(50),
    operation VARCHAR(200),
    type VARCHAR(20),
    method VARCHAR(10),
    params TEXT,
    ip VARCHAR(50),
    duration INT,
    success BOOLEAN DEFAULT TRUE,
    error_msg TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS search_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    keyword VARCHAR(200) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

MERGE INTO users (username, password_hash, role, status) KEY(username) VALUES
    ('admin', '$2a$10$XejjJoAUe4OwKQuC1OnfLOmGw0WQfHpLp6ZPK68x85LLrR48MuPKO', 'admin', 'active'),
    ('testuser', '$2a$10$XejjJoAUe4OwKQuC1OnfLOmGw0WQfHpLp6ZPK68x85LLrR48MuPKO', 'user', 'active');

MERGE INTO plants (name_cn, name_dong, category, usage_way, efficacy, view_count, favorite_count, popularity) KEY(name_cn) VALUES
    ('钩藤', 'Gouteng', '清热药', '内服', '清热平肝，息风止痉', 100, 10, 50),
    ('透骨草', 'Tougucao', '祛风湿药', '外用', '祛风除湿，活血止痛', 80, 5, 40),
    ('九节茶', 'Jiujiecha', '清热药', '内服', '清热解毒，消肿止痛', 60, 8, 35);

MERGE INTO knowledge (title, type, therapy_category, content, view_count, favorite_count, popularity) KEY(title) VALUES
    ('侗医药浴疗法', '疗法', '药浴', '侗族药浴是侗族人民在长期医疗实践中总结出的独特疗法', 200, 20, 80),
    ('侗医艾灸技术', '疗法', '艾灸', '艾灸是侗医常用外治法之一', 150, 15, 60);

MERGE INTO inheritors (name, level, bio, specialties, view_count, favorite_count, popularity) KEY(name) VALUES
    ('吴老师', '国家级', '侗医药非遗传承人', '药浴、艾灸', 300, 30, 90),
    ('杨师傅', '省级', '侗医药省级传承人', '草药辨识', 200, 20, 70);

MERGE INTO qa (category, question, answer, view_count, favorite_count, popularity) KEY(question) VALUES
    ('基础', '侗医药有什么特点？', '侗医药具有独特的理论体系', 100, 10, 50),
    ('疗法', '侗医药浴有哪些功效？', '祛风除湿、活血通络', 80, 8, 40);

MERGE INTO resources (title, category, description, view_count, download_count, favorite_count, popularity) KEY(title) VALUES
    ('侗医基础教程', '教程', '侗医药入门教程', 100, 50, 10, 60),
    ('侗药图鉴', '图鉴', '侗族药用植物图鉴', 80, 40, 8, 50);

MERGE INTO quiz_questions (question, options, answer, category, correct_answer, explanation) KEY(question) VALUES
    ('钩藤属于哪类中药？', '["清热药","祛风湿药","活血化瘀药","补虚药"]', 'A', '植物', 'A', '钩藤性凉，归肝经，具有清热平肝的功效'),
    ('侗医药浴的主要功效是？', '["祛风除湿","清热解毒","补气养血","安神定志"]', 'A', '疗法', 'A', '侗医药浴以祛风除湿为主要功效');

MERGE INTO comments (user_id, username, target_type, target_id, content, status, likes, reply_count) KEY(content) VALUES
    (2, 'testuser', 'plant', 1, '钩藤是非常好的药材', 'approved', 5, 0);

MERGE INTO favorites (user_id, target_type, target_id) KEY(user_id, target_type, target_id) VALUES
    (2, 'plant', 1);

MERGE INTO feedback (user_id, user_name, type, title, content, status) KEY(title) VALUES
    (2, 'testuser', 'suggestion', '测试反馈', '这是一条测试反馈', 'pending');
