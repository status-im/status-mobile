import requests
from bs4 import BeautifulSoup
import re


class LightweightBrowserHandler:

    def get_tx_details(self, tx_url: str) -> dict:
        """
        Extract the transaction fee and confirmation time from the given Etherscan transaction URL
        using lightweight tools and custom headers to mimic browser behavior.

        :param tx_url: The URL of the transaction page on Etherscan.
        :return: A tuple (tx_fee, confirmation_time) where tx_fee is a string (e.g., '0.00123 ETH')
                 and confirmation_time is a string (e.g., '1 min 14 secs').
        """
        try:
            # Custom headers to mimic a browser
            headers = {
                "User-Agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36",
                "Accept-Language": "en-US,en;q=0.9",
                "Cache-Control": "no-cache",
                "Connection": "keep-alive",
                "DNT": "1",  # Do Not Track request
            }

            # Make an HTTP GET request to fetch the HTML content
            response = requests.get(tx_url, headers=headers, timeout=10)
            response.raise_for_status()  # Raise an error for HTTP request failure

            # Parse HTML content using BeautifulSoup
            soup = BeautifulSoup(response.text, "html.parser")

            # Extract the transaction fee
            tx_fee_elem = soup.select_one("#txfeebutton")
            tx_fee = tx_fee_elem.text.strip() if tx_fee_elem else "Not Found"

            # Extract confirmation time
            confirmation_time_elem = soup.find(string=re.compile('Confirmed within'))

            confirmation_time = (
                confirmation_time_elem.text.replace("Confirmed within", "").strip()
                if confirmation_time_elem
                else "Not Found"
            )

            return {'fee': tx_fee, 'confirmation_time' : confirmation_time}
        except requests.RequestException as e:
            print(f"An error occurred while fetching transaction details: {e}")
            return {'fee': None, 'confirmation_time': None}


