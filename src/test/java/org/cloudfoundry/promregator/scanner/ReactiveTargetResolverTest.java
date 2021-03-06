package org.cloudfoundry.promregator.scanner;

import java.util.LinkedList;
import java.util.List;

import org.cloudfoundry.promregator.JUnitTestUtils;
import org.cloudfoundry.promregator.cfaccessor.CFAccessor;
import org.cloudfoundry.promregator.cfaccessor.CFAccessorMock;
import org.cloudfoundry.promregator.config.Target;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MockedReactiveTargetResolverSpringApplication.class)
public class ReactiveTargetResolverTest {
	@AfterClass
	public static void cleanupEnvironment() {
		JUnitTestUtils.cleanUpAll();
	}
	
	@After
	public void resetCFAccessorMock() {
		Mockito.reset(this.cfAccessor);
	}

	@Autowired
	private TargetResolver targetResolver;
	
	@Autowired
	private CFAccessor cfAccessor;

	@Test
	public void testFullyResolvedAlready() {
		
		List<Target> list = new LinkedList<>();
		
		Target t = new Target();
		t.setOrgName("unittestorg");
		t.setSpaceName("unittestspace");
		t.setApplicationName("testapp");
		t.setPath("path");
		t.setProtocol("https");
		list.add(t);
		
		List<ResolvedTarget> actualList = this.targetResolver.resolveTargets(list);
		
		Assert.assertEquals(1, actualList.size());
		
		ResolvedTarget rt = actualList.get(0);
		Assert.assertEquals(t, rt.getOriginalTarget());
		Assert.assertEquals(t.getOrgName(), rt.getOrgName());
		Assert.assertEquals(t.getSpaceName(), rt.getSpaceName());
		Assert.assertEquals(t.getApplicationName(), rt.getApplicationName());
		Assert.assertEquals(t.getPath(), rt.getPath());
		Assert.assertEquals(t.getProtocol(), rt.getProtocol());
		
		Mockito.verify(this.cfAccessor, Mockito.times(1)).retrieveAllApplicationIdsInSpace(CFAccessorMock.UNITTEST_ORG_UUID, CFAccessorMock.UNITTEST_SPACE_UUID);
		Mockito.verify(this.cfAccessor, Mockito.times(0)).retrieveSpaceSummary(CFAccessorMock.UNITTEST_SPACE_UUID);

	}
	
	@Test
	public void testMissingApplicationName() {
		List<Target> list = new LinkedList<>();
		
		Target t = new Target();
		t.setOrgName("unittestorg");
		t.setSpaceName("unittestspace");
		t.setPath("path");
		t.setProtocol("https");
		list.add(t);
		
		List<ResolvedTarget> actualList = this.targetResolver.resolveTargets(list);
		
		Assert.assertEquals(2, actualList.size());
		
		ResolvedTarget rt = actualList.get(0);
		Assert.assertEquals(t, rt.getOriginalTarget());
		Assert.assertEquals(t.getOrgName(), rt.getOrgName());
		Assert.assertEquals(t.getSpaceName(), rt.getSpaceName());
		Assert.assertEquals("testapp", rt.getApplicationName());
		Assert.assertEquals(t.getPath(), rt.getPath());
		Assert.assertEquals(t.getProtocol(), rt.getProtocol());
		
		rt = actualList.get(1);
		Assert.assertEquals(t, rt.getOriginalTarget());
		Assert.assertEquals(t.getOrgName(), rt.getOrgName());
		Assert.assertEquals(t.getSpaceName(), rt.getSpaceName());
		Assert.assertEquals("testapp2", rt.getApplicationName());
		Assert.assertEquals(t.getPath(), rt.getPath());
		Assert.assertEquals(t.getProtocol(), rt.getProtocol());
		
		Mockito.verify(this.cfAccessor, Mockito.times(1)).retrieveAllApplicationIdsInSpace(CFAccessorMock.UNITTEST_ORG_UUID, CFAccessorMock.UNITTEST_SPACE_UUID);
	}

	@Test
	public void testWithApplicationRegex() {
		List<Target> list = new LinkedList<>();
		
		Target t = new Target();
		t.setOrgName("unittestorg");
		t.setSpaceName("unittestspace");
		t.setApplicationRegex(".*2");
		t.setPath("path");
		t.setProtocol("https");
		list.add(t);
		
		List<ResolvedTarget> actualList = this.targetResolver.resolveTargets(list);
		
		Assert.assertEquals(1, actualList.size());
		
		ResolvedTarget rt = actualList.get(0);
		Assert.assertEquals(t, rt.getOriginalTarget());
		Assert.assertEquals(t.getOrgName(), rt.getOrgName());
		Assert.assertEquals(t.getSpaceName(), rt.getSpaceName());
		Assert.assertEquals("testapp2", rt.getApplicationName());
		Assert.assertEquals(t.getPath(), rt.getPath());
		Assert.assertEquals(t.getProtocol(), rt.getProtocol());
		
		Mockito.verify(this.cfAccessor, Mockito.times(1)).retrieveAllApplicationIdsInSpace(CFAccessorMock.UNITTEST_ORG_UUID, CFAccessorMock.UNITTEST_SPACE_UUID);
	}
	
	@Test
	public void testWithApplicationRegexCaseInsensitiveIssue76() {
		List<Target> list = new LinkedList<>();
		
		Target t = new Target();
		t.setOrgName("unittestorg");
		t.setSpaceName("unittestspace");
		t.setApplicationRegex("te.*App2");
		t.setPath("path");
		t.setProtocol("https");
		list.add(t);
		
		List<ResolvedTarget> actualList = this.targetResolver.resolveTargets(list);
		
		Assert.assertEquals(1, actualList.size());
		
		ResolvedTarget rt = actualList.get(0);
		Assert.assertEquals(t, rt.getOriginalTarget());
		Assert.assertEquals(t.getOrgName(), rt.getOrgName());
		Assert.assertEquals(t.getSpaceName(), rt.getSpaceName());
		Assert.assertEquals("testapp2", rt.getApplicationName());
		Assert.assertEquals(t.getPath(), rt.getPath());
		Assert.assertEquals(t.getProtocol(), rt.getProtocol());
		
		Mockito.verify(this.cfAccessor, Mockito.times(1)).retrieveAllApplicationIdsInSpace(CFAccessorMock.UNITTEST_ORG_UUID, CFAccessorMock.UNITTEST_SPACE_UUID);
	}
	
	@Test
	public void testEmpty() {
		
		List<Target> list = new LinkedList<>();
		
		List<ResolvedTarget> actualList = this.targetResolver.resolveTargets(list);
		
		Assert.assertNotNull(actualList);
		Assert.assertEquals(0, actualList.size());
	}

	@Test
	public void testSummaryDoesnotExist() {
		List<Target> list = new LinkedList<>();
		
		Target t = new Target();
		t.setOrgName("unittestorg");
		t.setSpaceName("unittestspace-summarydoesnotexist");
		t.setPath("path");
		t.setProtocol("https");
		list.add(t);
		
		List<ResolvedTarget> actualList = this.targetResolver.resolveTargets(list);
		
		Assert.assertEquals(0, actualList.size());
		
		Mockito.verify(this.cfAccessor, Mockito.times(1)).retrieveAllApplicationIdsInSpace(CFAccessorMock.UNITTEST_ORG_UUID, CFAccessorMock.UNITTEST_SPACE_UUID_DOESNOTEXIST);
	}
	
	@Test
	public void testRetrieveAllApplicationIdsInSpaceThrowsException() {
		List<Target> list = new LinkedList<>();
		
		Target t = new Target();
		t.setOrgName("unittestorg");
		t.setSpaceName("unittestspace-summaryexception");
		t.setPath("path");
		t.setProtocol("https");
		list.add(t);
		
		List<ResolvedTarget> actualList = this.targetResolver.resolveTargets(list);
		
		Assert.assertEquals(0, actualList.size());
		
		Mockito.verify(this.cfAccessor, Mockito.times(1)).retrieveAllApplicationIdsInSpace(CFAccessorMock.UNITTEST_ORG_UUID, CFAccessorMock.UNITTEST_SPACE_UUID_EXCEPTION);
	}
	
	@Test
	public void testInvalidOrgNameToResolve() {
		List<Target> list = new LinkedList<>();
		
		Target t = new Target();
		t.setOrgName("doesnotexist");
		t.setSpaceName("unittestspace");
		t.setPath("path");
		t.setProtocol("https");
		list.add(t);
		
		t = new Target();
		t.setOrgName("unittestorg");
		t.setSpaceName("unittestspace");
		t.setApplicationName("testapp2");
		t.setPath("path");
		t.setProtocol("https");
		list.add(t);
		
		List<ResolvedTarget> actualList = this.targetResolver.resolveTargets(list);
		
		Assert.assertEquals(1, actualList.size());
		
		ResolvedTarget rt = actualList.get(0);
		Assert.assertEquals(t, rt.getOriginalTarget());
		Assert.assertEquals(t.getOrgName(), rt.getOrgName());
		Assert.assertEquals(t.getSpaceName(), rt.getSpaceName());
		Assert.assertEquals(t.getApplicationName(), rt.getApplicationName());
		Assert.assertEquals(t.getPath(), rt.getPath());
		Assert.assertEquals(t.getProtocol(), rt.getProtocol());
	}
	
	@Test
	public void testExceptionOrgNameToResolve() {
		List<Target> list = new LinkedList<>();
		
		Target t = new Target();
		t.setOrgName("exception");
		t.setSpaceName("unittestspace");
		t.setPath("path");
		t.setProtocol("https");
		list.add(t);
		
		t = new Target();
		t.setOrgName("unittestorg");
		t.setSpaceName("unittestspace");
		t.setApplicationName("testapp2");
		t.setPath("path");
		t.setProtocol("https");
		list.add(t);
		
		List<ResolvedTarget> actualList = this.targetResolver.resolveTargets(list);
		
		Assert.assertEquals(1, actualList.size());
		
		ResolvedTarget rt = actualList.get(0);
		Assert.assertEquals(t, rt.getOriginalTarget());
		Assert.assertEquals(t.getOrgName(), rt.getOrgName());
		Assert.assertEquals(t.getSpaceName(), rt.getSpaceName());
		Assert.assertEquals(t.getApplicationName(), rt.getApplicationName());
		Assert.assertEquals(t.getPath(), rt.getPath());
		Assert.assertEquals(t.getProtocol(), rt.getProtocol());
	}
	
	@Test
	public void testInvalidSpaceNameToResolve() {
		List<Target> list = new LinkedList<>();
		
		Target t = new Target();
		t.setOrgName("unittestorg");
		t.setSpaceName("doesnotexist");
		t.setPath("path");
		t.setProtocol("https");
		list.add(t);
		
		t = new Target();
		t.setOrgName("unittestorg");
		t.setSpaceName("unittestspace");
		t.setApplicationName("testapp2");
		t.setPath("path");
		t.setProtocol("https");
		list.add(t);
		
		List<ResolvedTarget> actualList = this.targetResolver.resolveTargets(list);
		
		Assert.assertEquals(1, actualList.size());
		
		ResolvedTarget rt = actualList.get(0);
		Assert.assertEquals(t, rt.getOriginalTarget());
		Assert.assertEquals(t.getOrgName(), rt.getOrgName());
		Assert.assertEquals(t.getSpaceName(), rt.getSpaceName());
		Assert.assertEquals(t.getApplicationName(), rt.getApplicationName());
		Assert.assertEquals(t.getPath(), rt.getPath());
		Assert.assertEquals(t.getProtocol(), rt.getProtocol());
	}
	
	@Test
	public void testExceptionSpaceNameToResolve() {
		List<Target> list = new LinkedList<>();
		
		Target t = new Target();
		t.setOrgName("unittestorg");
		t.setSpaceName("exception");
		t.setPath("path");
		t.setProtocol("https");
		list.add(t);
		
		t = new Target();
		t.setOrgName("unittestorg");
		t.setSpaceName("unittestspace");
		t.setApplicationName("testapp2");
		t.setPath("path");
		t.setProtocol("https");
		list.add(t);
		
		List<ResolvedTarget> actualList = this.targetResolver.resolveTargets(list);
		
		Assert.assertEquals(1, actualList.size());
		
		ResolvedTarget rt = actualList.get(0);
		Assert.assertEquals(t, rt.getOriginalTarget());
		Assert.assertEquals(t.getOrgName(), rt.getOrgName());
		Assert.assertEquals(t.getSpaceName(), rt.getSpaceName());
		Assert.assertEquals(t.getApplicationName(), rt.getApplicationName());
		Assert.assertEquals(t.getPath(), rt.getPath());
		Assert.assertEquals(t.getProtocol(), rt.getProtocol());
	}

	@Test
	public void testDistinctResolvedTargets() {
		List<Target> list = new LinkedList<>();
		
		Target t = new Target();
		t.setOrgName("unittestorg");
		t.setSpaceName("unittestspace");
		t.setApplicationRegex("testapp.*");
		t.setPath("path");
		t.setProtocol("https");
		list.add(t);
		
		t = new Target();
		t.setOrgName("unittestorg");
		t.setSpaceName("unittestspace");
		t.setApplicationName("testapp");
		t.setPath("path");
		t.setProtocol("https");
		list.add(t);
		
		// NB: The regex target (first one) overlaps with the already fully resolved target (second one)
		
		List<ResolvedTarget> actualList = this.targetResolver.resolveTargets(list);
		
		Assert.assertEquals(2, actualList.size()); // and not 3!
		
		boolean testappFound = false;
		boolean testapp2Found = false;
		for (ResolvedTarget rt : actualList) {
			Assert.assertEquals(t.getOrgName(), rt.getOrgName());
			Assert.assertEquals(t.getSpaceName(), rt.getSpaceName());
			Assert.assertEquals(t.getPath(), rt.getPath());
			Assert.assertEquals(t.getProtocol(), rt.getProtocol());
			
			if (rt.getApplicationName().equals("testapp2")) {
				testapp2Found = true;
			} else if (rt.getApplicationName().equals("testapp")) {
				testappFound = true;
			} else {
				Assert.fail("Unknown application name returned");
			}
		}
		
		Assert.assertTrue(testappFound);
		Assert.assertTrue(testapp2Found);
	}

	@Test
	public void testMissingOrgName() {
		List<Target> list = new LinkedList<>();
		
		Target t = new Target();
		t.setSpaceName("unittestspace");
		t.setApplicationName("testapp");
		t.setPath("path");
		t.setProtocol("https");
		list.add(t);
		
		List<ResolvedTarget> actualList = this.targetResolver.resolveTargets(list);
		
		Assert.assertEquals(1, actualList.size());
		
		ResolvedTarget rt = actualList.get(0);
		Assert.assertEquals(t, rt.getOriginalTarget());
		Assert.assertEquals("unittestorg", rt.getOrgName());
		Assert.assertEquals(t.getSpaceName(), rt.getSpaceName());
		Assert.assertEquals("testapp", rt.getApplicationName());
		Assert.assertEquals(t.getPath(), rt.getPath());
		Assert.assertEquals(t.getProtocol(), rt.getProtocol());
		
		Mockito.verify(this.cfAccessor, Mockito.times(1)).retrieveAllOrgIds();
		Mockito.verify(this.cfAccessor, Mockito.times(1)).retrieveAllApplicationIdsInSpace(CFAccessorMock.UNITTEST_ORG_UUID, CFAccessorMock.UNITTEST_SPACE_UUID);
	}

	@Test
	public void testWithOrgRegex() {
		List<Target> list = new LinkedList<>();
		
		Target t = new Target();
		t.setOrgRegex("unittest.*");
		t.setSpaceName("unittestspace");
		t.setApplicationName("testapp2");
		t.setPath("path");
		t.setProtocol("https");
		list.add(t);
		
		List<ResolvedTarget> actualList = this.targetResolver.resolveTargets(list);
		
		Assert.assertEquals(1, actualList.size());
		
		ResolvedTarget rt = actualList.get(0);
		Assert.assertEquals(t, rt.getOriginalTarget());
		Assert.assertEquals("unittestorg", rt.getOrgName());
		Assert.assertEquals(t.getSpaceName(), rt.getSpaceName());
		Assert.assertEquals("testapp2", rt.getApplicationName());
		Assert.assertEquals(t.getPath(), rt.getPath());
		Assert.assertEquals(t.getProtocol(), rt.getProtocol());
		
		Mockito.verify(this.cfAccessor, Mockito.times(1)).retrieveAllOrgIds();
		Mockito.verify(this.cfAccessor, Mockito.times(1)).retrieveAllApplicationIdsInSpace(CFAccessorMock.UNITTEST_ORG_UUID, CFAccessorMock.UNITTEST_SPACE_UUID);
	}
	
	@Test
	public void testWithOrgRegexCaseInsensitive() {
		List<Target> list = new LinkedList<>();
		
		Target t = new Target();
		t.setOrgRegex("unit.*TOrg");
		t.setSpaceName("unittestspace");
		t.setApplicationName("testapp2");
		t.setPath("path");
		t.setProtocol("https");
		list.add(t);
		
		List<ResolvedTarget> actualList = this.targetResolver.resolveTargets(list);
		
		Assert.assertEquals(1, actualList.size());
		
		ResolvedTarget rt = actualList.get(0);
		Assert.assertEquals(t, rt.getOriginalTarget());
		Assert.assertEquals("unittestorg", rt.getOrgName());
		Assert.assertEquals(t.getSpaceName(), rt.getSpaceName());
		Assert.assertEquals("testapp2", rt.getApplicationName());
		Assert.assertEquals(t.getPath(), rt.getPath());
		Assert.assertEquals(t.getProtocol(), rt.getProtocol());
		
		Mockito.verify(this.cfAccessor, Mockito.times(1)).retrieveAllApplicationIdsInSpace(CFAccessorMock.UNITTEST_ORG_UUID, CFAccessorMock.UNITTEST_SPACE_UUID);
	}
	
	@Test
	public void testMissingSpaceName() {
		List<Target> list = new LinkedList<>();
		
		Target t = new Target();
		t.setOrgName("unittestorg");
		t.setApplicationName("testapp");
		t.setPath("path");
		t.setProtocol("https");
		list.add(t);
		
		List<ResolvedTarget> actualList = this.targetResolver.resolveTargets(list);
		
		Assert.assertEquals(1, actualList.size());
		
		ResolvedTarget rt = actualList.get(0);
		Assert.assertEquals(t, rt.getOriginalTarget());
		Assert.assertEquals("unittestorg", rt.getOrgName());
		Assert.assertEquals("unittestspace", rt.getSpaceName());
		Assert.assertEquals("testapp", rt.getApplicationName());
		Assert.assertEquals(t.getPath(), rt.getPath());
		Assert.assertEquals(t.getProtocol(), rt.getProtocol());
		
		Mockito.verify(this.cfAccessor, Mockito.times(1)).retrieveSpaceIdsInOrg(CFAccessorMock.UNITTEST_ORG_UUID);
		Mockito.verify(this.cfAccessor, Mockito.times(1)).retrieveAllApplicationIdsInSpace(CFAccessorMock.UNITTEST_ORG_UUID, CFAccessorMock.UNITTEST_SPACE_UUID);
	}

	@Test
	public void testWithSpaceRegex() {
		List<Target> list = new LinkedList<>();
		
		Target t = new Target();
		t.setOrgName("unittestorg");
		t.setSpaceRegex("unitte.*");
		t.setApplicationName("testapp2");
		t.setPath("path");
		t.setProtocol("https");
		list.add(t);
		
		List<ResolvedTarget> actualList = this.targetResolver.resolveTargets(list);
		
		Assert.assertEquals(1, actualList.size());
		
		ResolvedTarget rt = actualList.get(0);
		Assert.assertEquals(t, rt.getOriginalTarget());
		Assert.assertEquals("unittestorg", rt.getOrgName());
		Assert.assertEquals("unittestspace", rt.getSpaceName());
		Assert.assertEquals("testapp2", rt.getApplicationName());
		Assert.assertEquals(t.getPath(), rt.getPath());
		Assert.assertEquals(t.getProtocol(), rt.getProtocol());
		
		Mockito.verify(this.cfAccessor, Mockito.times(1)).retrieveSpaceIdsInOrg(CFAccessorMock.UNITTEST_ORG_UUID);
		Mockito.verify(this.cfAccessor, Mockito.times(1)).retrieveAllApplicationIdsInSpace(CFAccessorMock.UNITTEST_ORG_UUID, CFAccessorMock.UNITTEST_SPACE_UUID);
	}
	
	@Test
	public void testWithSpaceRegexCaseInsensitive() {
		List<Target> list = new LinkedList<>();
		
		Target t = new Target();
		t.setOrgName("unittestorg");
		t.setSpaceRegex("unit.*tSpace");
		t.setApplicationName("testapp2");
		t.setPath("path");
		t.setProtocol("https");
		list.add(t);
		
		List<ResolvedTarget> actualList = this.targetResolver.resolveTargets(list);
		
		Assert.assertEquals(1, actualList.size());
		
		ResolvedTarget rt = actualList.get(0);
		Assert.assertEquals(t, rt.getOriginalTarget());
		Assert.assertEquals("unittestorg", rt.getOrgName());
		Assert.assertEquals("unittestspace", rt.getSpaceName());
		Assert.assertEquals("testapp2", rt.getApplicationName());
		Assert.assertEquals(t.getPath(), rt.getPath());
		Assert.assertEquals(t.getProtocol(), rt.getProtocol());
		
		Mockito.verify(this.cfAccessor, Mockito.times(1)).retrieveSpaceIdsInOrg(CFAccessorMock.UNITTEST_ORG_UUID);
		Mockito.verify(this.cfAccessor, Mockito.times(1)).retrieveAllApplicationIdsInSpace(CFAccessorMock.UNITTEST_ORG_UUID, CFAccessorMock.UNITTEST_SPACE_UUID);
	}
	
	@Test
	public void testCorrectingCaseOnNamesIssue77() {
		
		List<Target> list = new LinkedList<>();
		
		Target t = new Target();
		t.setOrgName("uniTtEstOrg");
		t.setSpaceName("unitteStspAce");
		t.setApplicationName("testApp");
		t.setPath("path");
		t.setProtocol("https");
		list.add(t);
		
		List<ResolvedTarget> actualList = this.targetResolver.resolveTargets(list);
		
		Assert.assertEquals(1, actualList.size());
		
		ResolvedTarget rt = actualList.get(0);
		Assert.assertEquals(t, rt.getOriginalTarget());
		Assert.assertEquals("unittestorg", rt.getOrgName());
		Assert.assertEquals("unittestspace", rt.getSpaceName());
		Assert.assertEquals("testapp", rt.getApplicationName());
		Assert.assertEquals(t.getPath(), rt.getPath());
		Assert.assertEquals(t.getProtocol(), rt.getProtocol());

	}
	
	@Test
	public void testInvalidOrgNameDoesNotRaiseExceptionIssue109() {
		List<Target> list = new LinkedList<>();
		
		Target t = new Target();
		t.setOrgName("doesnotexist");
		list.add(t);
		
		List<ResolvedTarget> actualList = this.targetResolver.resolveTargets(list);
		
		Assert.assertEquals(0, actualList.size());
		
	}

	@Test
	public void testInvalidSpaceNameDoesNotRaiseExceptionIssue109() {
		
		List<Target> list = new LinkedList<>();
		
		Target t = new Target();
		t.setOrgName("unittestorg");
		t.setSpaceName("doesnotexist");
		list.add(t);
		
		List<ResolvedTarget> actualList = this.targetResolver.resolveTargets(list);
		
		Assert.assertEquals(0, actualList.size());
	}

}
