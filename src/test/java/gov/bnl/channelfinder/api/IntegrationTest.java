package gov.bnl.channelfinder.api;

import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class IntegrationTest {

	/**
	 * Rigourous Test :-)
	 */

	private static ChannelFinderClient client;
	@BeforeClass
	public static void setUp() throws Exception {
		client = ChannelFinderClient.getInstance();
		// set up the preferences
//		Preferences preferences = Preferences
//				.userNodeForPackage(ChannelFinderClient.class);
//		preferences
//				.put("channel_finder_url",
//						"https://channelfinder.nsls2.bnl.gov:8181/ChannelFinder/resources");
//		preferences.put("username", "boss");
//		preferences.put("password", "****");
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDown() throws Exception {
	}

	/**
	 * use GET to acquire a list of channels
	 */
	@Test
	public void getChannels() {
		try {
			XmlChannels chs = client.retrieveChannels();
			System.out.println(chs.getChannels().size());
			assertTrue(chs.getChannels().size() >= 0);
		} catch (ChannelFinderException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getStatus().getStatusCode() + " "
					+ e.getStatus() + "\n" + e.getMessage());
			// e.printStackTrace();
		}
	}

	/**
	 * Add single channel pvk01
	 */

	@Test
	public void addremoveChannel() {
		try {
			XmlChannel channel = new XmlChannel("pvk01:<first>", "boss");
			client.addChannel(channel);
			XmlChannels chs = client.retrieveChannels();
			assertTrue(chs.containsKey("pvk01:<first>"));
			client.removeChannel("pvk01:<first>");
			chs = client.retrieveChannels();
			assertTrue(!chs.getChannels().contains(channel));
		} catch (ChannelFinderException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getStatus().getStatusCode() + e.getMessage());
		}
	}

	/**
	 * Add and remove channels
	 */
	@Test
	public void addremoveChannels() {
		XmlChannels chs = new XmlChannels();
		for (int i = 1; i <= 3; i++) {
			chs.addChannel(new XmlChannel("pvk0" + i, "shroffk"));
		}
		// add
		client.addChannels(chs);
		XmlChannels rchs = client.retrieveChannels();
		int count = rchs.getChannels().size();
		for (int i = 1; i <= 3; i++) {
			assertTrue(rchs.containsKey("pvk0" + i));
		}
		// remove
		client.removeChannels(chs.getChannelNames());
		rchs = client.retrieveChannels();
		for (int i = 1; i <= 3; i++) {
			assertTrue(!rchs.containsKey("pvk0" + i));
		}
		// check 3 channels were removed
		assertTrue((count - rchs.getChannels().size()) == 3);
		// assertTrue(rchs.retrieveChannels().size() == 0);
	}

	/**
	 * 
	 */

	/**
	 * update an existing channel with a new property and new tag
	 * 
	 * Add/update test
	 */
	@Test
	public void updateChannel() {
		XmlChannel channel = new XmlChannel("pvk03", "shroffk");
		client.addChannel(channel);
		XmlChannel retChannel = client.retreiveChannel(
				channel.getName());
		// check for no initial properties or tags
		assertTrue(retChannel.getXmlProperties().size() == 0);
		assertTrue(retChannel.getXmlTags().size() == 0);
		channel.addProperty(new XmlProperty("prop1", "shroffk", "val1"));
		channel.addTag(new XmlTag("tag1", "shroffk"));
		// uses the POST method
		client.updateChannel(channel);
		assertTrue(client.retreiveChannel(
				channel.getName()).getXmlProperties().size() == 1);
		assertTrue(client.retreiveChannel(
				channel.getName()).getXmlTags().size() == 1);
		client.removeChannel(channel.getName());
	}

	/**
	 * Test destructive update - existing channel is completely replaced
	 * 
	 */
	@Test
	public void addChannel() {
		XmlChannel oldChannel = new XmlChannel("old", "shroffk");
		oldChannel.addTag(new XmlTag("old", "shroffk"));
		client.addChannel(oldChannel);
		assertTrue(client.queryChannelsByTag("old")
				.getChannels().size() == 1);
		XmlChannel newChannel = new XmlChannel("old", "shroffk");
		newChannel.addTag(new XmlTag("new", "shroffk"));
		client.addChannel(newChannel);
		assertTrue(client.queryChannelsByTag("old")
				.getChannels().size() == 0);
		assertTrue(client.queryChannelsByTag("new")
				.getChannels().size() == 1);
		client.removeChannel(newChannel.getName());
	}

	/**
	 * Test set Tag to all the channels specified. The tag already exists in the
	 * database
	 */
	@Test
	public void setTag() {
		XmlChannels chs = new XmlChannels();
		XmlChannel ch = new XmlChannel("pvk01", "boss");
		XmlTag tag = new XmlTag("tagName", "shroffk");
		ch.addTag(tag);
		chs.addChannel(ch);
		chs.addChannel(new XmlChannel("pvk02", "boss"));
		chs.addChannel(new XmlChannel("pvk03", "boss"));
		client.addChannels(chs);
		assertTrue(client.queryChannelsByTag(
				tag.getName()).getChannels().size() == 1);
		client.resetTag(chs.getChannelNames(), tag);
		assertTrue(client.queryChannelsByTag("tagName").getChannels().size() == 3);
		client.removeChannels(chs.getChannelNames());
	}

	/**
	 * Test set Tag to all the channels specified. The tag ownership details are
	 * present in the payload
	 */
	@Test
	public void setTag2() {
		XmlChannels chs = new XmlChannels();
		for (int i = 1; i <= 3; i++) {
			chs.addChannel(new XmlChannel("pvk0" + i, "boss"));
		}
		client.addChannels(chs);

		XmlTag tag = new XmlTag("tagName", "shroffk");
		// adding owner to the payload.
//		chs.getChannels().toArray(new XmlChannel[0])[0].addTag(tag);
		assertTrue(client.queryChannelsByTag(tag.getName()).getChannels().size() == 0);
		client.resetTag(chs.getChannelNames(), tag);
		assertTrue(client.queryChannelsByTag(tag.getName()).getChannels().size() == 3);
		client.removeChannels(chs.getChannelNames());

	}

	/**
	 * test the correct operation of both methods to add tags. addTag() - non
	 * destructive - POST. resetTag() - destructive - PUT. The tag ownership
	 * details are already present in the database
	 */
	@Test
	public void addTag() {
		XmlChannels chs = new XmlChannels();
		XmlChannels results;
		XmlChannel ch = new XmlChannel("pvk01", "boss");
		XmlTag tag = new XmlTag("tagName", "boss");
		ch.addTag(tag);
		chs.addChannel(ch);
		for (int i = 2; i <= 4; i++) {
			chs.addChannel(new XmlChannel("pvk0" + i, "boss"));
		}
		client.addChannels(chs);

		// Add a tag to channels using post - this should leave other tags
		// intact
		// tagged channels include pvk01
		results = client.queryChannelsByTag(
				tag.getName());
		assertTrue(results.getChannels().size() == 1);

		chs = new XmlChannels();
		chs.addChannel(new XmlChannel("pvk02", "boss"));
		client.addTag(chs.getChannelNames(), tag);

		// tagged channels include pvk01, pvk02
		results = client.queryChannelsByTag(
				tag.getName());
		assertTrue(results.getChannels().size() == 2);
		for (XmlChannel channel : results.getChannels()) {
			assertTrue(channel.getName().equals("pvk01")
					|| channel.getName().equals("pvk02"));
		}

		chs = new XmlChannels();
		chs.addChannel(new XmlChannel("pvk03", "boss"));
		chs.addChannel(new XmlChannel("pvk04", "boss"));
		client.resetTag(chs.getChannelNames(), tag);

		// tagged channels include pvk03 and 04 - tag removed from pvk01 and 02
		results = client.queryChannelsByTag(
				tag.getName());
		assertTrue(results.getChannels().size() == 2);
		for (XmlChannel channel : results.getChannels()) {
			assertTrue(channel.getName().equals("pvk03")
					|| channel.getName().equals("pvk04"));
		}
		for (int i = 1; i <= 4; i++) {
			client.removeChannel("pvk0" + i);
		}
	}

	/**
	 * Remove tag from all channels
	 */
	@Test
	public void removeTag() {
		XmlChannel ch;
		XmlTag tag = new XmlTag("tagName", "boss");
		for (int i = 1; i <= 3; i++) {
			ch = new XmlChannel("pvk0" + i, "boss");
			ch.addTag(tag);
			client.addChannel(ch);
		}
		assertTrue(client.queryChannelsByTag(tag.getName()).getChannels().size() == 3);
		client.deleteTag(tag.getName());
		assertTrue(client.queryChannelsByTag(tag.getName()).getChannels().size() == 0);
		// cleanup
		for (int i = 1; i <= 3; i++) {
			client.removeChannel("pvk0" + i);
		}

	}

	/**
	 * Test adding and removing a tag on a single channel
	 */
	@Test
	public void addRemoveChannelTag() {
		XmlChannel channel = new XmlChannel("pvk01", "boss");
		client.addChannel(channel);

		// Add tag
		XmlTag tag = new XmlTag("tagName", "boss");
		client.resetTag(channel.getName(), tag);
		assertTrue(client.queryChannelsByTag(tag.getName()).getChannels().size() == 1);
		// Remove tag
		client.removeTag(channel.getName(), tag.getName());
		assertTrue(client.queryChannelsByTag(tag.getName()).getChannels().size() == 0);
		// cleanup
		client.removeChannel(channel.getName());
	}

	/**
	 * Test adding and removing properties
	 * 
	 */
	@Test
	public void addRemoveProps() {
		XmlProperty prop1 = new XmlProperty("prop1", "boss", "1");
		XmlChannel channel = new XmlChannel("pvk01", "boss");
		int matches = client.queryChannelsByProp(prop1.getName()).getChannels().size();
		
		// add a property to single channels
		client.addChannel(channel);
		client.addProperty(channel.getName(), prop1);
		assertTrue(client.queryChannelsByProp(prop1.getName()).getChannels().size() == (matches+1));
		
		// add a property to a set of channels
		XmlChannels channels = new XmlChannels();
		channels.addChannel(new XmlChannel("pvk02", "boss"));
		channels.addChannel(new XmlChannel("pvk03", "boss"));
		client.addChannels(channels);
		client.addProperty(channels.getChannelNames(), prop1);
		assertTrue(client.queryChannelsByProp(prop1.getName()).getChannels().size() == (matches+3));
		
		// remove a property wherever it might appear
		XmlProperty prop2 = new XmlProperty("prop2", "boss", "2");
		client.addProperty(channels.getChannelNames(), prop2);
		assertTrue(client.queryChannelsByProp("prop2").getChannels().size() == 2);
		client.deleteProperty("prop2");
		assertTrue(client.queryChannelsByProp("prop2").getChannels().size() == 0);
		
		// remove a property from a single channel
		client.removeProperty(channel.getName(), prop1.getName());
		assertTrue(client.queryChannelsByProp(prop1.getName()).getChannels().size() == (matches+2));
		
		// remove a property from a set of channels
		client.removeProperty(channels.getChannelNames(), prop1.getName());
		assertTrue(client.queryChannelsByProp(prop1.getName()).getChannels().size() == (matches));

		client.removeChannel(channel.getName());
		client.removeChannels(channels.getChannelNames());
	}
}