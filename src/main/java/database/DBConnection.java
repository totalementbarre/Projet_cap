package database;


import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

public class DBConnection {
    private SessionFactory sessionFactory;
    private Configuration configuration;

    public DBConnection() {

        StandardServiceRegistry standardRegistry = new StandardServiceRegistryBuilder()
                .configure()
                .build();


        // Mapping classes
        this.configuration = new Configuration()
                .addAnnotatedClass(UserInfos.class)
                .configure();

        Metadata metadata = new MetadataSources(standardRegistry)
                .addAnnotatedClass(UserInfos.class)
                .getMetadataBuilder()
                .applyImplicitNamingStrategy(ImplicitNamingStrategyJpaCompliantImpl.INSTANCE)
                .build();


        this.sessionFactory = metadata.getSessionFactoryBuilder()
                .applyBeanManager(getBeanManager())
                .build();


    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    private Object getBeanManager() {
        return null;
    }
}
