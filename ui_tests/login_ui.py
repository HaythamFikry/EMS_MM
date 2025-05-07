import unittest
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.chrome.options import Options
import time,json,urllib

class MyEventsPageTest(unittest.TestCase):
    @classmethod
    def setUp(cls):
        with open("ui_tests/config.json") as f:
            config = json.load(f)
            cls.BASE_URL = config["BASE_URL"]
            cls.LOGIN_URL = urllib.parse.urljoin(cls.BASE_URL, config["LOGIN_URL"])
            CREATED_USER = config["CREATED_USER_ATTENDEE"] or config["CREATED_USER_ORGANIZER"] or {}
            cls.USERNAME = CREATED_USER.get("username", "soliman")
            cls.PASSWORD = CREATED_USER.get("password", "123")

        
        cls.options = Options()
        cls.options.add_argument("--start-maximized")

        cls.driver = webdriver.Chrome(options=cls.options)
        cls.driver.get(cls.LOGIN_URL)

    def test_successful_login(self):
        self.driver.get(self.LOGIN_URL)
        username = self.driver.find_elements(By.CSS_SELECTOR, 'input[ui_test="login-username"]')
        self.assertTrue(username, "Username field not found")
        username[0].send_keys(self.USERNAME)

        password = self.driver.find_elements(By.CSS_SELECTOR, 'input[ui_test="login-password"]')
        self.assertTrue(password, "Password field not found")
        password[0].send_keys(self.PASSWORD)
        password[0].send_keys(Keys.RETURN)

        time.sleep(2)

        welcome_div = self.driver.find_elements(By.CSS_SELECTOR, 'div.welcome')
        self.assertTrue(welcome_div, "Welcome message not found after login")

    def test_failed_login_invalid_credentials(self):
        
        self.driver.get(self.LOGIN_URL)

        username = self.driver.find_elements(By.CSS_SELECTOR, 'input[ui_test="login-username"]')
        self.assertTrue(username, "Username field not found")
        username[0].send_keys("wronguser")

        password = self.driver.find_elements(By.CSS_SELECTOR, 'input[ui_test="login-password"]')
        self.assertTrue(password, "Password field not found")
        password[0].send_keys("wrongpassword")
        password[0].send_keys(Keys.RETURN)

        time.sleep(2)

        # Check if error message is displayed
        error_div = self.driver.find_elements(By.CSS_SELECTOR, 'div.alert.alert-danger[role="alert"]')
        self.assertTrue(error_div, "Error message not displayed for invalid login")

    @classmethod
    def tearDownClass(cls):
        cls.driver.quit()


if __name__ == "__main__":
    unittest.main()
