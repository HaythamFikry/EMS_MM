import unittest, time, faker, json,urllib
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.chrome.options import Options

class TestRegisterForm(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        with open("ui_tests/config.json") as f:
            cls.config = json.load(f)
            cls.BASE_URL = cls.config["BASE_URL"]
            cls.REGISTER_URL = urllib.parse.urljoin(cls.BASE_URL, cls.config["REGISTER_URL"])
            cls.LOGOUT_URL = urllib.parse.urljoin(cls.BASE_URL, cls.config["LOGOUT_URL"])

        cls.options = Options()
        cls.options.add_argument("--start-maximized")
        cls.driver = webdriver.Chrome(options=cls.options)
        cls.driver.get(cls.REGISTER_URL)

    def test_register_new_user_attendee(self):
        fake = faker.Faker()

        if self.driver.find_elements(By.CSS_SELECTOR, 'a[href*="/logout"]'):
            self.driver.find_element(By.CSS_SELECTOR, 'a[href*="/logout"]').click()
            time.sleep(2)            
            self.driver.get(self.REGISTER_URL)

        self.driver.find_element(By.CSS_SELECTOR, "[ui_test='register-first-name']").send_keys(fake.first_name())
        self.driver.find_element(By.CSS_SELECTOR, "[ui_test='register-last-name']").send_keys(fake.last_name())
        email = fake.email()
        self.driver.find_element(By.CSS_SELECTOR, "[ui_test='register-email']").send_keys(email)
        username = fake.user_name()
        self.driver.find_element(By.CSS_SELECTOR, "[ui_test='register-username']").send_keys(username)

        password = "TestPass123!"
        self.driver.find_element(By.CSS_SELECTOR, "[ui_test='register-password']").send_keys(password)
        self.driver.find_element(By.CSS_SELECTOR, "[ui_test='register-confirm-password']").send_keys(password)

        role = "Attendee"
        role_select = self.driver.find_element(By.CSS_SELECTOR, "[ui_test='register-role']")
        role_select.send_keys(role)

        terms_checkbox = self.driver.find_element(By.CSS_SELECTOR, "[ui_test='register-terms']")
        self.driver.execute_script("arguments[0].click();", terms_checkbox)

        self.register_button = self.driver.find_element(By.CSS_SELECTOR, "[ui_test='register-button']")
        self.driver.execute_script("arguments[0].click();", self.register_button)

        time.sleep(3)

        # Assertion: Registration redirects to login or dashboard
        self.assertTrue(
            "/login" in self.driver.current_url or "events" in self.driver.current_url.lower(),
            f"Registration failed or unexpected redirect: {self.driver.current_url}"
        )
        print("created user:", username, email, password, role)
        self.config["CREATED_USER_ATTENDEE"] = {"username": username, "email": email, "password": password,"role": role}
        with open("ui_tests/config.json", "w") as f:
            json.dump(self.config, f, indent=4)


    def test_register_new_user_organizer(self):
        fake = faker.Faker()

        if self.driver.find_elements(By.CSS_SELECTOR, 'a[href*="/logout"]'):
            self.driver.find_element(By.CSS_SELECTOR, 'a[href*="/logout"]').click()
            time.sleep(2)
            self.driver.get(self.REGISTER_URL)

        self.driver.find_element(By.CSS_SELECTOR, "[ui_test='register-first-name']").send_keys(fake.first_name())
        self.driver.find_element(By.CSS_SELECTOR, "[ui_test='register-last-name']").send_keys(fake.last_name())
        email = fake.email()
        self.driver.find_element(By.CSS_SELECTOR, "[ui_test='register-email']").send_keys(email)
        username = fake.user_name()
        self.driver.find_element(By.CSS_SELECTOR, "[ui_test='register-username']").send_keys(username)

        password = "TestPass123!"
        self.driver.find_element(By.CSS_SELECTOR, "[ui_test='register-password']").send_keys(password)
        self.driver.find_element(By.CSS_SELECTOR, "[ui_test='register-confirm-password']").send_keys(password)

        role = "Event Organizer"
        role_select = self.driver.find_element(By.CSS_SELECTOR, "[ui_test='register-role']")
        role_select.send_keys(role)

        terms_checkbox = self.driver.find_element(By.CSS_SELECTOR, "[ui_test='register-terms']")
        self.driver.execute_script("arguments[0].click();", terms_checkbox)

        self.register_button = self.driver.find_element(By.CSS_SELECTOR, "[ui_test='register-button']")
        self.driver.execute_script("arguments[0].click();", self.register_button)

        time.sleep(3)

        # Assertion: Registration redirects to login or dashboard
        self.assertTrue(
            "/login" in self.driver.current_url or "events" in self.driver.current_url.lower(),
            f"Registration failed or unexpected redirect: {self.driver.current_url}"
        )
        print("created user:", username, email, password,role)
        self.config["CREATED_USER_ORGANIZER"] = {"username": username, "email": email, "password": password,"role": role}
        with open("ui_tests/config.json", "w") as f:
            json.dump(self.config, f, indent=4)

    @classmethod
    def tearDownClass(cls):
        cls.driver.quit()

if __name__ == "__main__":
    unittest.main()
