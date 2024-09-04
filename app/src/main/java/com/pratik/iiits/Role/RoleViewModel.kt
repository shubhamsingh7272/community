package com.pratik.iiits.Role

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class RoleViewModel : ViewModel() {

    private val roleRepository = RoleRepository()
    private val _roles = MutableLiveData<List<RoleRequest>>()
    val roles: LiveData<List<RoleRequest>> get() = _roles

    init {
        fetchRolesByUserId()
    }

    private fun fetchRolesByUserId() {
        viewModelScope.launch {
            val rolesList = roleRepository.getRolesByUserId()
            _roles.postValue(rolesList)
        }
    }
}
