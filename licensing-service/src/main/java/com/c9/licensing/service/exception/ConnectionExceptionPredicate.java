package com.c9.licensing.service.exception;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.function.Predicate;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;

public class ConnectionExceptionPredicate {
	
	private ConnectionExceptionPredicate() {
	}

	public static final Predicate<Throwable> isConnectionBasedException = throwable -> {
		Throwable cause = throwable.getCause();
		if (cause == null) {
			return false;
		}
		Class<? extends Throwable> causeClass = cause.getClass();
		return causeClass.isAssignableFrom(HttpHostConnectException.class)
				|| causeClass.isAssignableFrom(ConnectTimeoutException.class)
				|| causeClass.isAssignableFrom(SocketTimeoutException.class)
				|| causeClass.isAssignableFrom(UnknownHostException.class);
	};
}
