package ru.prmo.exception

import org.springframework.http.HttpStatus

class CharactersInIntegerField : BaseException(
    httpStatus = HttpStatus.BAD_REQUEST,
    apiError = ApiError(
        errorCode = "characters.in.integer.field",
        description = "������ �� ����� ������� � ����, ������� ��������� ������ �������� ��������. ����������� ������������� ��������� � ���������� ��� ���..."
    )
) {
}