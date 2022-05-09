insert into person (id, first_name, last_name, reg_date, birth_date, e_mail, phone, password, city, country) values
    (1, 'Test', 'Test', '1649367846500', '1649367846000', 'test@mail.ru', '+7(111)1111111',
     '$2a$10$2bACTJ0LEKT8E36hgAu.x./IrdIe7B5S08K5vwQ2FHmZ/DoEJ1RLS', 'Saratov', 'Russia'),
    (2, 'Ivan', 'Ivanov', '1649367846500', '1649367846000', 'ivanov@mail.ru', '+7(999)9999999',
     '$2a$10$2bACTJ0LEKT8E36hgAu.x./IrdIe7B5S08K5vwQ2FHmZ/DoEJ1RLS', 'Tver', 'Russia'),
    (3, 'Petr', 'Petrov', '1649367846500', '1649367846000', 'petrov@mail.ru', '+7(999)9999999',
     '$2a$10$2bACTJ0LEKT8E36hgAu.x./IrdIe7B5S08K5vwQ2FHmZ/DoEJ1RLS', 'Kaliningrad', 'Russia'),
    (4, 'Tihon', 'Tihonov', '1649367846500', '1649367846000', 'tihonov@mail.ru', '+7(999)9999999',
     '$2a$10$2bACTJ0LEKT8E36hgAu.x./IrdIe7B5S08K5vwQ2FHmZ/DoEJ1RLS', 'Perm', 'Russia'),
    (5, 'Ilya', 'Ilin', '1649367846500', '1649367846000', 'ilin@mail.ru', '+7(999)9999999',
     '$2a$10$2bACTJ0LEKT8E36hgAu.x./IrdIe7B5S08K5vwQ2FHmZ/DoEJ1RLS', 'Tbilisi', 'Georgia'),
    (6, 'Onufriy', 'Onufriev', '1649367846500', '1649367846000', 'onufriy@mail.ru', '+7(999)9999999',
     '$2a$10$2bACTJ0LEKT8E36hgAu.x./IrdIe7B5S08K5vwQ2FHmZ/DoEJ1RLS', 'Alma-Ata', 'Kazakhstan'),
    (7, 'Onufriy', 'Testoviy', '1649367846500', '1649367846000', 'onufriy1@mail.ru', '+7(999)9999999',
    '$2a$10$2bACTJ0LEKT8E36hgAu.x./IrdIe7B5S08K5vwQ2FHmZ/DoEJ1RLS', 'Alma-Ata', 'Kazakhstan');

insert into post (id, time, author, title, post_text, is_blocked, likes) values
    (1, '1649367846501', 1, 'test title example', 'test post text example', 'false', 2),
    (2, '1649367846502', 2, 'ivan title example', 'ivan post text example', 'false', 1),
    (3, '1649367846503', 3, 'petr title example', 'petr post text example', 'false', 1),
    (4, '1649367846504', 4, 'tihon title example', 'tihon post text example', 'false', 1),
    (5, '1649367846505', 5, 'ilya title example', 'ilya post text example', 'false', 1);

insert into post_like (id, time, person_id, post_id) values
    (1, '1649367846501', 1, 1),
    (2, '1649367846501', 1, 2),
    (3, '1649367846501', 1, 3),
    (4, '1649367846501', 1, 4),
    (5, '1649367846501', 2, 1);

insert into tag (tag) values ('top15'), ('java'), ('code'), ('true_story'), ('spring'), ('core'), ('bootstrap');

insert into post2tag (id, post_id, tag_id) values
    (1, 1, 1),
    (2, 1, 2),
    (3, 1, 3),
    (4, 1, 4),
    (5, 1, 5),
    (6, 1, 6),
    (7, 1, 7);

insert into message (time, author_id, recipient_id, message_text, read_status, dialog_id) values
    ('1649367846501', 2, 1, 'test', 'SENT', 1),
    ('1649367846502', 2, 1, 'test1', 'SENT', 1),
    ('1649367846503', 3, 1, 'test2', 'SENT', 2),
    ('1649367846504', 4, 1, 'test3', 'SENT', 3),
    ('1649367846505', 5, 1, 'test4', 'READ', 4),
    ('1649367846506', 5, 1, 'test5', 'READ', 4),
    ('1649367846507', 6, 1, 'test6', 'SENT', 5),
    ('1649367846508', 6, 1, 'test7', 'SENT', 5);

INSERT INTO dialog (id) values
    (1), (2), (3), (4), (5);

insert into post_comment (time, post_id, author_id, comment_text, is_blocked) values
    ('1649367846508', 1, 5, 'test title example', false),
    ('1649367846508', 2, 6, 'ivan title example', false),
    ('1649367846508', 3, 1, 'petr title example', false),
    ('1649367846508', 4, 5, 'tihon title example', false),
    ('1649367846508', 5, 2, 'ilya title example', false),
    ('1649367846508', 6, 3, 'onufriy title example', false),
    ('1649367846508', 6, 5, 'test title example 2', false),
    ('1649367846508', 2, 4, 'ivan title example 2', false),
    ('1649367846508', 3, 2, 'petr title example 2', false),
    ('1649367846508', 3, 1, 'tihon title example 2', false);