package com.devlambda.meta.persistence;


import static org.junit.Assert.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.IntConsumer;

import org.junit.BeforeClass;
import org.junit.Test;

import com.devlambda.meta.Type;
import com.devlambda.meta.persistence.domain.school.Student;
import com.devlambda.meta.persistence.domain.school.StudentClassMap;
import com.devlambda.meta.persistence.domain.school.Subject;


/**
 * Many to many Graph example using the {@link Student} to {@link Subject} relationship.
 * 
 * @author Dewald Pretorius
 */
public class ManyToManyIT {

   static final String JDBC_URL = "jdbc:h2:mem:metarepo;DB_CLOSE_DELAY=-1";
   static final String SQL_CREATE_STUDENT = 
         "CREATE TABLE IF NOT EXISTS STUDENT(" + 
            "ID BIGINT NOT NULL AUTO_INCREMENT, " + 
            "NAME VARCHAR(200) NOT NULL, " + 
            "CONSTRAINT PK_STUDENT PRIMARY KEY(ID))";
   static final String SQL_CREATE_CLASS = 
         "CREATE TABLE IF NOT EXISTS CLASS(" +
            "ID BIGINT NOT NULL AUTO_INCREMENT, " + 
            "CODE CHAR(6) NOT NULL, " +
            "NAME VARCHAR(100) NOT NULL, " + 
            "CONSTRAINT UK_SUBJECT_CODE UNIQUE(CODE), " +
            "CONSTRAINT PK_SUBJECT PRIMARY KEY(ID))";
   static final String SQL_CREATE_MAP = 
         "CREATE TABLE IF NOT EXISTS STUDENT_CLASS_MAP(" + 
            "STUDENT_ID BIGINT NOT NULL, " + 
            "CLASS_ID BIGINT NOT NULL, " + 
            "CONSTRAINT PK_STUDENT_CLASS_MAP KEY(STUDENT_ID, CLASS_ID), " +
            "CONSTRAINT FK_SC_MAP_STUDENT FOREIGN KEY(STUDENT_ID) REFERENCES STUDENT(ID)," +
            "CONSTRAINT FK_SC_MAP_CLASS FOREIGN KEY(CLASS_ID) REFERENCES CLASS(ID))";
   
   static final String SQL_SELECT = SQL.select("", "s.ID", "s.NAME", "c.ID", "c.CODE", "c.NAME");
   
   static final String QUERY_JOIN_ALL = "JOIN ALL";
   static final String SQL_QUERY_JOIN_ALL = SQL_SELECT + 
         "FROM STUDENT AS s " + 
         "INNER JOIN STUDENT_CLASS_MAP AS m ON m.STUDENT_ID = s.ID " +
         "INNER JOIN CLASS AS c ON c.ID = m.CLASS_ID";
   
   static final Type<Student> TYPE_STUDENT = Type.meta(Student::new);
   static final Type<Subject> TYPE_CLASS = Type.meta(Subject::new);
   static final Type<StudentClassMap> TYPE_MAP = Type.meta(StudentClassMap::new);
   static final GraphBuilder GRAPH = GraphBuilder.builder();
   
   static MPA mpa;
   static List<Long> studentIds = new ArrayList<>();
   static List<Long> classIds = new ArrayList<>();

   @BeforeClass
   public static void bootUp() throws Exception {
      setupMetaTypes();
      setupMPA();
      setupData();
   }

   private static void setupMetaTypes() {
      TYPE_STUDENT.add("ID", Long.class, Student::getId, Student::setId)
                  .add("NAME", String.class, Student::getName, Student::setName);
      TYPE_CLASS.add("ID", Long.class, Subject::getId, Subject::setId)
                .add("CODE", String.class, Subject::getCode, Subject::setCode)
                .add("NAME", String.class, Subject::getName, Subject::setName);
      TYPE_MAP.add("STUDENT_ID", Long.class, StudentClassMap::getLeft, StudentClassMap::setLeft)
              .add("CLASS_ID", Long.class, StudentClassMap::getRight, StudentClassMap::setRight);
      
      GRAPH.mapTypeProperty(0, "ID", TYPE_STUDENT, "student")
           .mapTypeProperty(1, "NAME", TYPE_STUDENT, "student")
           .mapTypeProperty(2, "ID", TYPE_CLASS, "class")
           .mapTypeProperty(3, "CODE", TYPE_CLASS, "class")
           .mapTypeProperty(4, "NAME", TYPE_CLASS, "class");
      
      BiConsumer<Subject, Student> studentAdder = (subject, student) -> subject.getStudents().add(student);
      GRAPH.mapGraphProperty("student", studentAdder, TYPE_CLASS, "class");
      BiConsumer<Student, Subject> classAdder = (student, subject) -> student.getSubjects().add(subject);
      GRAPH.mapGraphProperty("class", classAdder, TYPE_STUDENT, "student");
   }

   private static void setupMPA() throws SQLException {
      mpa = new MPA(Connector.connectionProvider(JDBC_URL));

      try (Statement studentStatement = mpa.connection().createStatement();
           Statement classStatement = mpa.connection().createStatement();
           Statement mapStatement = mpa.connection().createStatement();) {
         studentStatement.execute(SQL_CREATE_STUDENT);
         classStatement.execute(SQL_CREATE_CLASS);
         mapStatement.execute(SQL_CREATE_MAP);
      }
   }

   private static void setupData() {
      mpa.register(TYPE_STUDENT, Statement.RETURN_GENERATED_KEYS);
      mpa.register(TYPE_CLASS, "CLASS", Statement.RETURN_GENERATED_KEYS);
      mpa.register(TYPE_MAP, "STUDENT_CLASS_MAP", new ArrayList<>(TYPE_MAP.getPropertyNames()), 
                   Statement.NO_GENERATED_KEYS);
      mpa.setQuery(QUERY_JOIN_ALL, SQL_QUERY_JOIN_ALL);
      
      mpa.begin();
      
      studentIds.add(mpa.persist(new Student("Dewald Pretorius")).getId());
      studentIds.add(mpa.persist(new Student("Ahsoka Tano")).getId());
      studentIds.add(mpa.persist(new Student("Jan van Riebeeck")).getId());
      studentIds.add(mpa.persist(new Student("Isaac Newton")).getId());
      studentIds.add(mpa.persist(new Student("Nicolaus Copernicus")).getId());
      studentIds.add(mpa.persist(new Student("Winston Churchill")).getId());
      studentIds.add(mpa.persist(new Student("Martin Luther King Jr.")).getId());
      studentIds.add(mpa.persist(new Student("Abraham Lincoln")).getId());
      studentIds.add(mpa.persist(new Student("Aristotle")).getId());
      studentIds.add(mpa.persist(new Student("Leonardo da Vinci")).getId());
      studentIds.add(mpa.persist(new Student("Galileo Galilei")).getId());
      studentIds.add(mpa.persist(new Student("Wolfgang Amadeus Mozart")).getId());
      studentIds.add(mpa.persist(new Student("Marco Polo")).getId());
      
      classIds.add(mpa.persist(new Subject("AFR101", "Afrikaans")).getId());
      classIds.add(mpa.persist(new Subject("MAT201", "Mathematics")).getId());
      classIds.add(mpa.persist(new Subject("ENG203", "Engineering")).getId());
      classIds.add(mpa.persist(new Subject("JAV243", "Programming")).getId());
      classIds.add(mpa.persist(new Subject("SQL543", "Database Design")).getId());
      classIds.add(mpa.persist(new Subject("WWW546", "Web Design")).getId());
      classIds.add(mpa.persist(new Subject("TCP443", "Network Communications")).getId());
      classIds.add(mpa.persist(new Subject("ART345", "Art")).getId());
      
      Random random = new Random();
      
      for (Long classId : classIds) {
         IntConsumer mapRelation = index -> mpa.persist(new StudentClassMap(studentIds.get(index), classId));
         int streamSize = random.nextInt(studentIds.size());
         random.ints(streamSize, 0, studentIds.size()).distinct().forEach(mapRelation);
      }
      
      mpa.commit();
   }
   
   @Test
   public void testGetStudents() {
      List<Student> students = mpa.findAll(Student.class);
      assertEquals(studentIds.size(), students.size());
   }
   
   @Test
   public void testGetClasses() {
      List<Subject> subjects = mpa.findAll(Subject.class);
      assertEquals(classIds.size(), subjects.size());
   }
   
   @Test
   public void testGetStudentClassMap() {
      List<StudentClassMap> relations = mpa.findAll(StudentClassMap.class);
      assertFalse(relations.isEmpty());
   }
   
   @Test
   public void testGraphQueryJoinAll() throws SQLException {
      Query query = mpa.getQuery(QUERY_JOIN_ALL);
      PreparedStatement statement = query.get();
      
      try (ResultSet resultSet = statement.executeQuery();) {
         GraphResult result = GRAPH.build(mpa, resultSet);
         
         Set<Student> students = result.getResults("student", Student.class);
         Set<Subject> subjects = result.getResults("class", Subject.class);
         
         assertFalse(students.isEmpty());
         assertFalse(subjects.isEmpty());
         
         for (Subject subject : subjects) {
            for (Student student : subject.getStudents()) {
               assertTrue(student.getSubjects().contains(subject));
            }
         }
         
         for (Student student : students) {
            for (Subject subject : student.getSubjects()) {
               assertTrue(subject.getStudents().contains(student));
            }
         }
      }
   }
}
