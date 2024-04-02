package ru.prmo.service

import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.prmo.dto.UserRegistrationDto
import ru.prmo.entity.UserEntity
import ru.prmo.repository.RoleRepository
import ru.prmo.repository.UserRepository

@Service
class UserService(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val passwordEncoder: PasswordEncoder,
    private val departmentService: DepartmentService
): UserDetailsService {

    fun findByUsername(username: String): UserEntity {
        return userRepository.findByUsername(username)
    }
    @Throws(UsernameNotFoundException::class)
    @Transactional
    override fun loadUserByUsername(username: String): UserDetails {
        val user: UserEntity = findByUsername(username)

        return User(
            user.username,
            user.password,
            user.roles.map { roleEntity -> SimpleGrantedAuthority(roleEntity.roleName) }
        )
    }

    fun createNewUser(userRegistrationDto: UserRegistrationDto) {
        val newUser = UserEntity(
            username = userRegistrationDto.username!!,
            password = passwordEncoder.encode(userRegistrationDto.password),
            department = departmentService.getDepartmentByName(userRegistrationDto.department!!),
            roles = listOf(roleRepository.findByRoleName("ROLE_USER"))
        )
        userRepository.save(newUser)
    }
}