
server {
    server_name test.mygoldbucks.com;

    location / {
        proxy_pass http://localhost:3173;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # Proxy pass for backend API
    location /tc/ {
        proxy_pass http://localhost:8020;
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



    listen 443 ssl; # managed by Certbot
    ssl_certificate /etc/letsencrypt/live/test.mygoldbucks.com/fullchain.pem; # managed by Certbot
    ssl_certificate_key /etc/letsencrypt/live/test.mygoldbucks.com/privkey.pem; # managed by Certbot
    include /etc/letsencrypt/options-ssl-nginx.conf; # managed by Certbot
    ssl_dhparam /etc/letsencrypt/ssl-dhparams.pem; # managed by Certbot

}

server {


    if ($host = test.mygoldbucks.com) {
        return 301 https://$host$request_uri;
    } # managed by Certbot


    listen 80;
    server_name test.mygoldbucks.com;
    return 404; # managed by Certbot


}
