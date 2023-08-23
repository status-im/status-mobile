(ns status-im2.contexts.chat.composer.link-preview.events-test
  (:require [status-im2.contexts.chat.composer.link-preview.events :as events]
            [cljs.test :refer [is deftest testing]]))

(def url-github "https://github.com")
(def url-gitlab "https://gitlab.com")
(def preview-github {:url url-github :thumbnail nil})
(def preview-gitlab {:url url-gitlab :thumbnail nil})
(def request-id "abc123")

(defn remove-rpc-callbacks
  [effects]
  (if (get-in effects [:json-rpc/call 0])
    (update-in effects [:json-rpc/call 0] dissoc :on-success :on-error)
    effects))

(deftest unfurl-urls
  (testing "clear up state when text is empty"
    (let [cofx {:db {:chat/link-previews {:unfurled   {}
                                          :cache      {}
                                          :request-id "123"}}}]
      (is (= {:db {:chat/link-previews {:cache {}}}}
             (events/unfurl-urls cofx " ")))))

  (testing "fetches parsed URLs"
    (let [cofx {:db {:chat/link-previews {:unfurled   {}
                                          :cache      {}
                                          :request-id "123"}}}]
      (is (= {:json-rpc/call [{:method "wakuext_getTextURLs"
                               :params [url-github]}]}
             (remove-rpc-callbacks
              (events/unfurl-urls cofx url-github)))))))

(deftest unfurl-parsed-urls-test
  (with-redefs [events/new-request-id (constantly request-id)]
    (testing "empty state with 2 stale URLs"
      (let [cofx {:db {:chat/link-previews
                       {:cache    {}
                        :unfurled []}}}]
        (is (= {:db            {:chat/link-previews
                                {:request-id request-id
                                 :cache      {}
                                 :unfurled   [{:url url-github :loading? true}
                                              {:url url-gitlab :loading? true}]}}
                :json-rpc/call [{:method "wakuext_unfurlURLs"
                                 :params [[url-github url-gitlab]]}]}
               (remove-rpc-callbacks
                (events/unfurl-parsed-urls cofx [url-github url-gitlab]))))))

    (testing "nothing unfurled, 1 cached URL and the other stale"
      (let [cache {url-github preview-github}
            cofx  {:db {:chat/link-previews
                        {:cache    cache
                         :unfurled []}}}]
        (is (= {:db            {:chat/link-previews
                                {:request-id request-id
                                 :cache      cache
                                 :unfurled   [preview-github
                                              {:url url-gitlab :loading? true}]}}
                :json-rpc/call [{:method "wakuext_unfurlURLs"
                                 :params [[url-gitlab]]}]}
               (remove-rpc-callbacks
                (events/unfurl-parsed-urls cofx [url-github url-gitlab]))))))

    (testing "does nothing when the URLs are identical to the last cleared ones"
      (let [cofx {:db {:chat/link-previews
                       {:cache    {url-github preview-github}
                        :cleared  #{url-github url-gitlab}
                        :unfurled [preview-github preview-gitlab]}}}]
        (is (nil? (events/unfurl-parsed-urls cofx [url-github url-gitlab])))))

    (testing "app db has previews, but the new parsed text has no valid URLs"
      (let [cache {url-github preview-github}
            cofx  {:db {:chat/link-previews
                        {:cache    cache
                         :unfurled [preview-github]}}}]
        (is (= {:db {:chat/link-previews
                     {:request-id request-id
                      :cache      cache
                      :unfurled   []}}}
               (remove-rpc-callbacks
                (events/unfurl-parsed-urls cofx []))))))

    (testing "2 unfurled, 2 cached URLs"
      (let [cache {url-github preview-github url-gitlab preview-gitlab}
            cofx  {:db {:chat/link-previews
                        {:cache    cache
                         :unfurled [preview-github preview-gitlab]}}}]
        (is (= {:db {:chat/link-previews
                     {:request-id request-id
                      :cache      cache
                      :unfurled   [preview-github preview-gitlab]}}}
               (remove-rpc-callbacks
                (events/unfurl-parsed-urls cofx [url-github url-gitlab]))))))

    (testing "1 unfurled, 1 successful in the cache, 1 failed in the cache"
      (let [cache {url-github preview-github
                   url-gitlab (assoc preview-gitlab :failed? true)}
            cofx  {:db {:chat/link-previews
                        {:cache    cache
                         :unfurled [preview-github]}}}]
        (is (= {:db {:chat/link-previews
                     {:request-id request-id
                      :cache      cache
                      :unfurled   [preview-github]}}}
               (events/unfurl-parsed-urls cofx [url-github url-gitlab])))))

    (testing "nothing unfurled, 1 failed in the cache"
      (let [cache {url-gitlab (assoc preview-gitlab :failed? true)}
            cofx  {:db {:chat/link-previews
                        {:cache    cache
                         :unfurled []}}}]
        (is (= {:db            {:chat/link-previews
                                {:request-id request-id
                                 :cache      cache
                                 :unfurled   [{:url url-github :loading? true}]}}
                :json-rpc/call [{:method "wakuext_unfurlURLs"
                                 :params [[url-github]]}]}
               (remove-rpc-callbacks
                (events/unfurl-parsed-urls cofx [url-github url-gitlab]))))))

    (testing "empties unfurled collection when there are no valid previews"
      (let [unknown-url "https://github.abcdef"
            cache       {unknown-url {:url unknown-url :failed? true}
                         url-github  preview-github}
            cofx        {:db {:chat/link-previews
                              {:cache    cache
                               :unfurled [preview-github]}}}]
        (is (= {:db {:chat/link-previews
                     {:request-id request-id
                      :cache      cache
                      :unfurled   []}}}
               (remove-rpc-callbacks
                (events/unfurl-parsed-urls cofx [unknown-url]))))))))

(deftest unfurl-parsed-urls-success-test
  (testing "does nothing if the success request-id is different than the current one"
    (let [cofx {:db {:chat/link-previews
                     {:request-id request-id
                      :unfurled   []
                      :cache      {}}}}]
      (is (nil? (events/unfurl-parsed-urls-success cofx "banana" [preview-github])))))

  (testing "reconciles new previews with existing ones"
    (let [cofx     {:db {:chat/link-previews
                         {:request-id request-id
                          :unfurled   [preview-github
                                       {:url url-gitlab :loading? true}]
                          :cache      {url-github preview-github}}}}
          {db :db} (events/unfurl-parsed-urls-success cofx
                                                      request-id
                                                      [preview-gitlab])]
      (is (= {:chat/link-previews
              {:request-id request-id
               :unfurled   [preview-github preview-gitlab]
               :cache      {url-github preview-github
                            url-gitlab preview-gitlab}}}
             db))))

  (testing "identify and write failed preview in the cache"
    (let [preview-youtube {:url "https://youtube.com" :thumbnail nil}
          cofx            {:db {:chat/link-previews
                                {:request-id request-id
                                 :unfurled   [{:url url-github :loading? true}
                                              preview-youtube
                                              {:url url-gitlab :loading? true}]
                                 :cache      {(:url preview-youtube) preview-youtube}}}}
          {db :db}        (events/unfurl-parsed-urls-success cofx
                                                             request-id
                                                             [preview-github
                                                              preview-youtube])]
      (is (= {:chat/link-previews
              {:request-id request-id
               :unfurled   [preview-github preview-youtube]
               :cache      {(:url preview-youtube) preview-youtube
                            url-github             preview-github
                            url-gitlab             {:url "https://gitlab.com" :failed? true}}}}
             db)))))

(deftest clear-link-previews-test
  (let [cache   {url-github preview-github}
        effects (events/clear-link-previews
                 {:db {:chat/link-previews {:unfurled   [preview-github]
                                            :request-id request-id
                                            :cache      cache}}})]
    (is (= {:db {:chat/link-previews {:cache   cache
                                      :cleared #{url-github}}}}
           effects))))

(deftest reset-unfurled-test
  (let [cache {url-github preview-github}]
    (is (= {:db {:chat/link-previews {:cache cache}}}
           (events/reset-unfurled
            {:db {:chat/link-previews {:unfurled   [preview-github]
                                       :request-id request-id
                                       :cleared    #{url-github}
                                       :cache      cache}}})))))

(deftest reset-all-test
  (is (= {:db {:non-related-key :some-value}}
         (events/reset-all
          {:db {:non-related-key    :some-value
                :chat/link-previews {:request-id    request-id
                                     :any-other-key "farewell"}}}))))
