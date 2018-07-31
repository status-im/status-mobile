(ns status-im.test.utils.http
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.utils.http :as http]))

(deftest url-sanitize-check
  (testing "https://storage.googleapis.com/ck-kitty-image/0x06012c8cf97bead5deae237070f9587f8e7a266d/818934.svg"
    (testing "it returns true"
      (is (http/url-sanitized? "https://storage.googleapis.com/ck-kitty-image/0x06012c8cf97bead5deae237070f9587f8e7a266d/818934.svg"))))

  (testing "https://www.cryptostrikers.com/assets/images/cards/017.svg"
    (testing "it returns true"
      (is (http/url-sanitized? "https://www.cryptostrikers.com/assets/images/cards/017.svg"))))

  (testing "https://www.etheremon.com/assets/images/mons_origin/025.png"
    (testing "it returns true"
      (is (http/url-sanitized? "https://www.etheremon.com/assets/images/mons_origin/025.png"))))

  (testing "http://www.etheremon.com/assets/images/mons_origin/025.png"
    (testing "it returns false"
      (is (not (http/url-sanitized? "http://www.etheremon.com/assets/images/mons_origin/025.png")))))

  (testing "xxx:x \\\\x0Aonerror=javascript:alert(1)"
    (testing "it returns false"
      (is (not (http/url-sanitized? "xxx:x \\\\x0Aonerror=javascript:alert(1)")))))

  (testing "https://www.etheremon.com/assets/images/mons_origin/025.png'&lt;script&gt;alert(&#39;123&#39;);&lt;/script&gt;"
    (testing "it returns false"
      (is (not (http/url-sanitized? "https://www.etheremon.com/assets/images/mons_origin/025.png'&lt;script&gt;alert(&#39;123&#39;);&lt;/script&gt;")))))

  (testing "https://www.etheremon.com/assets/images/mons'&lt;script&gt;alert(&#39;123&#39;);&lt;/script&gt;origin/025.png"
    (testing "it returns false"
      (is (not (http/url-sanitized? "https://www.etheremon.com/assets/images/mons'&lt;script&gt;alert(&#39;123&#39;);&lt;/script&gt;origin/025.png")))))

  (testing "https://www.etheremon.com/assets/images/mons_origin/025.png'><script>\\\\x3Bjavascript:alert(1)</script>"
    (testing "it returns false"
      (is (not (http/url-sanitized? "https://www.etheremon.com/assets/images/mons_origin/025.png'><script>\\\\x3Bjavascript:alert(1)</script>")))))

  (testing "https://www.etheremon.com/assets/images/mons'><script>\\\\x3Bjavascript:alert(1)</script>origin/025.png"
    (testing "it returns false"
      (is (not (http/url-sanitized? "https://www.etheremon.com/assets/images/mons'><script>\\\\x3Bjavascript:alert(1)</script>origin/025.png"))))))

(deftest url-host-check
  (testing "Extract host/domain from URL"
    (testing "Valid URL with endpoint"
      (is (= "status.im" (http/url-host "https://status.im/testing"))))
    (testing "Valid URL"
      (is (= "status.im" (http/url-host "http://status.im"))))
    (testing "Blank domainlocalhost"
      (is (nil? (http/url-host "localhost:3000/testing")))))
  (testing "Return nil for Invalid URL"
    (testing "Bad scheme"
      (is (nil? (http/url-host "invalid//status.im/testing"))))
    (testing "No scheme"
      (is (nil? (http/url-host "status.im/testing"))))))

