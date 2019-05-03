package io.micronaut.data.hibernate

import io.micronaut.context.annotation.Property
import io.micronaut.test.annotation.MicronautTest
import spock.lang.Specification
import spock.lang.Stepwise

import javax.inject.Inject

@MicronautTest(rollback = false)
@Property(name = "datasources.default.name", value = "mydb")
@Property(name = 'jpa.default.properties.hibernate.hbm2ddl.auto', value = 'create-drop')
@Stepwise
class CrudRepositorySpec extends Specification {

    @Inject
    PersonCrudRepository crudRepository

    void "test save one"() {
        when:"one is saved"
        def person = new Person(name: "Fred")
        crudRepository.save(person)

        then:"the instance is persisted"
        person.id != null
        crudRepository.findById(person.id).isPresent()
        crudRepository.existsById(person.id)
    }

    void "test save many"() {
        when:"many are saved"
        def p1 = new Person(name: "Frank")
        def p2 = new Person(name: "Bob")
        def people = [p1, p2]
        crudRepository.saveAll(people)

        then:"all are saved"
        people.every { it.id != null }
        people.every { crudRepository.findById(it.id).isPresent() }
    }
}