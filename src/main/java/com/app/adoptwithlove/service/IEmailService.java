package com.app.adoptwithlove.service;

public interface IEmailService {

    void sendEmail(String[] toUser, String subject, String message);

}
