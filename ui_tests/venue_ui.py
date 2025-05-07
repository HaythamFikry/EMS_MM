import unittest, time, faker, random,json,urllib
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.chrome.options import Options

class TestAddVenueForm(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        
        with open("ui_tests/config.json") as f:
            config = json.load(f)
            cls.BASE_URL = config["BASE_URL"]
            cls.LOGIN_URL = urllib.parse.urljoin(cls.BASE_URL, config["LOGIN_URL"])
            cls.LIST_VENUE_URL = urllib.parse.urljoin(cls.BASE_URL, config["LIST_VENUE_URL"])
            cls.ADD_VENUE_URL = urllib.parse.urljoin(cls.BASE_URL, config["ADD_VENUE_URL"])
            CREATED_USER = config["CREATED_USER_ATTENDEE"] or config["CREATED_USER_ORGANIZER"] or {}
            cls.USERNAME = CREATED_USER.get("username", "soliman")
            cls.PASSWORD = CREATED_USER.get("password", "123")


        cls.options = Options()
        cls.options.add_argument("--start-maximized")

        cls.driver = webdriver.Chrome(options=cls.options)
        cls.driver.get(cls.LOGIN_URL)

        time.sleep(1)
        # Log in the user
        username = cls.driver.find_elements(By.CSS_SELECTOR, 'input[ui_test="login-username"]')
        cls.assertTrue(username, "Username field not found")
        username[0].send_keys(cls.USERNAME)

        password = cls.driver.find_elements(By.CSS_SELECTOR, 'input[ui_test="login-password"]')
        cls.assertTrue(password, "Password field not found")
        password[0].send_keys(cls.PASSWORD)
        password[0].send_keys(Keys.RETURN)

        time.sleep(2)

        welcome_div = cls.driver.find_elements(By.CSS_SELECTOR, 'div.welcome')
        cls.assertTrue(welcome_div, "Welcome message not found after login")



    # def test_add_venue_redirect(self):
    #     print("test_add_venue_redirect")
    #     self.driver.get(self.LIST_VENUE_URL)
    #     # Wait for the page to redirect
    #     time.sleep(2)

    #     # Click the add button
    #     add_button = self.driver.find_element(By.XPATH, "//a[@ui_test='add-venue-button']")
    #     add_button.click()

    #     # Wait for the page to redirect
    #     time.sleep(2)
    #     # Assert that the URL is correct
    #     print(self.driver.current_url)
    #     self.assertEqual(self.driver.current_url, self.ADD_VENUE_URL)
    
    def test_add_5_venues(self):
        fake = faker.Faker()        
        venues = []
        for _ in range(3):
            venue = {
                "name": fake.company(),
                "address": fake.address().replace("\n", " "),
                "capacity": random.randint(1, 999),
                "contact_person": fake.name(),
                "contact_phone": random.randint(1000000000, 9999999999),
                "contact_email": fake.email()
            }
            venues.append(venue)

        for venue in venues:
            self.driver.get(self.ADD_VENUE_URL)
            
            time.sleep(2)
            # Fill in the form inputs
            name_input = self.driver.find_element(By.CSS_SELECTOR, "[ui_test='add-venue-name']")
            name_input.send_keys(venue["name"])

            address_input = self.driver.find_element(By.CSS_SELECTOR, "[ui_test='add-venue-address']")
            address_input.send_keys(venue["address"])
            capacity_input = self.driver.find_element(By.CSS_SELECTOR, "[ui_test='add-venue-capacity']")
            capacity_input.send_keys(venue["capacity"])

            contact_person_input = self.driver.find_element(By.CSS_SELECTOR, "[ui_test='add-venue-contact-person']")
            contact_person_input.send_keys(venue["contact_person"])

            contact_phone_input = self.driver.find_element(By.CSS_SELECTOR, "[ui_test='add-venue-contact-phone']")
            contact_phone_input.send_keys(venue["contact_phone"])

            contact_email_input = self.driver.find_element(By.CSS_SELECTOR, "[ui_test='add-venue-contact-email']")
            contact_email_input.send_keys(venue["contact_email"])

            # Click the submit button
            submit_button = self.driver.find_element(By.XPATH, "//button[@ui_test='submit-venue-button']")
            self.driver.execute_script("arguments[0].click();", submit_button)

            # Wait for the page to redirect
            time.sleep(2)
            # Assert that the URL is correct
            print(self.driver.current_url)
            self.assertEqual(self.driver.current_url, self.LIST_VENUE_URL)

            # Assert that the inserted title is present
            # title_element = self.driver.find_element(By.XPATH, "//div[@class='venue-card-text']/h5[contains(., 'Test Venue')]")
            # self.assertIsNotNone(title_element)

    @classmethod
    def tearDownClass(cls):
        cls.driver.quit()

if __name__ == "__main__":
    unittest.main()