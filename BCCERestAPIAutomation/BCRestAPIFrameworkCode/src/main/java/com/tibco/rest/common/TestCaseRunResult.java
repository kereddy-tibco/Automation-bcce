package com.tibco.rest.common;

public enum TestCaseRunResult {
	PASS("Pass"), FAIL("Fail"), SKIPPED("Not Run");

	private String testlink;

	private TestCaseRunResult(String toTestLink) {
		testlink = toTestLink;
	}

	public String toTestLink() {
		return testlink;
	}
}
