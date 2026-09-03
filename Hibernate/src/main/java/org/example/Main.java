package org.example;

import jakarta.persistence.Query;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
public class Main {
    private static final StandardServiceRegistry registry =
            new StandardServiceRegistryBuilder().configure("hibernate.cfg.xml").build();
    private static final Metadata metadata = new MetadataSources(registry).getMetadataBuilder().build();
    private static final SessionFactory sessionFactory = metadata.getSessionFactoryBuilder().build();
    private static final Logger LOGGER = LogManager.getLogger(Main.class);
    public static void main(String[] args) {

        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();

        try {
            String hql = "insert into LinkedPurchaseList (studentId, courseId) " +
                         "select distinct s.id, c.id from PurchaseList pl " +
                         "join Student s on pl.studentName = s.name " +
                         "join Subscription s2 on s.id = s2.studentId " +
                         "join Course c on pl.courseName = c.name";
            Query query = session.createQuery(hql);
            query.executeUpdate();
        } catch (Exception e)  {
            LOGGER.info(e.getMessage());
        } finally {
            transaction.commit();
            session.close();
            sessionFactory.close();
        }
    }
}