update admin_user
set password_hash = '$2a$10$m78y.vIGDEeT96nG3HQu7.9MkeWaUgeKxOk4UrIxN1NampZ2bRSKq'
where username = 'admin'
  and password_hash = 'admin123';

update member_user
set password_hash = '$2a$10$tqj4zrOyB6WLyiNokC2x0.ATPer5VtIwcHlpGimcmkg2zVQd10KpC'
where username = 'member01'
  and password_hash = '123456';
