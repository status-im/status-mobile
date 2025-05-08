import requests
from bs4 import BeautifulSoup
import re


class LightweightBrowserHandler:
    
    def __init__(self, base_url: str):
        self.base_url = base_url


    def load_tx_etherscan_page(self, tx_hash: str) -> BeautifulSoup:
        headers = {
            "User-Agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36",
            "Accept-Language": "en-US,en;q=0.9",
            "Cache-Control": "no-cache",
            "Connection": "keep-alive",
            "DNT": "1",
        }
        tx_url = '%s/tx/%s' % (self.base_url, tx_hash)
        response = requests.get(tx_url, headers=headers, timeout=10)
        response.raise_for_status()
        return BeautifulSoup(response.text, "html.parser")

    def extract_tx_details(self, soup: BeautifulSoup) -> dict:
        tx_fee_elem = soup.select_one("#txfeebutton")
        tx_fee = tx_fee_elem.text.strip() if tx_fee_elem else "Not Found"

        confirmation_time_elem = soup.find(string=re.compile('Confirmed within'))
        confirmation_time = (
            confirmation_time_elem.text.replace("Confirmed within", "").strip()
            if confirmation_time_elem
            else "Not Found"
        )

        return {'fee': tx_fee, 'confirmation_time': confirmation_time}

    def find_text(self, soup: BeautifulSoup, text: str) -> bool:
        return soup.find(string=re.compile(re.escape(text))) is not None

    def find_success_badge(self, soup: BeautifulSoup):
        for span in soup.find_all("span", class_=lambda x: x and "text-green-600" in x):
            if "Success" in span.get_text():
                return span
        return None

    def get_fee_and_confirmation_time(self, tx_url: str) -> dict:
        try:
            soup = self.load_tx_etherscan_page(tx_url)
            return self.extract_tx_details(soup)
        except requests.RequestException as e:
            print(f"An error occurred while fetching transaction details: {e}")
            return {'fee': None, 'confirmation_time': None}
    