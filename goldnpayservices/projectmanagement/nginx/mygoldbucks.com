
server {
    server_name mygoldbucks.com;

    location / {
        proxy_pass http://localhost:4273;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # Proxy pass for backend API
    location /tc/ {
        proxy_pass http://localhost:8090;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # Location block to handle Let's Encrypt ACME challenges
    location ^~ /.well-known/acme-challenge/ {
        root /var/www/html;
        # Adjust the path to your web root directory
        default_type "text/plain";
    }



}

server {


    listen 80;
    server_name mygoldbucks.com;
    


}
