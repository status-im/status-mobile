(ns status-im2.contexts.chat.composer.link-preview.events
  (:require
    [clojure.set :as set]
    [clojure.string :as string]
    [legacy.status-im.data-store.messages :as data-store.messages]
    [taoensso.timbre :as log]
    [utils.collection]
    [utils.re-frame :as rf]))

(rf/defn unfurl-urls
  {:events [:link-preview/unfurl-urls]}
  [{:keys [db]} text]
  (if (string/blank? text)
    {:db (update db :chat/link-previews dissoc :unfurled :request-id)}
    {:json-rpc/call
     [{:method     "wakuext_getTextURLs"
       :params     [text]
       :on-success #(rf/dispatch [:link-preview/unfurl-parsed-urls %])
       :on-error   (fn [error]
                     (log/error "Failed to parse text and extract URLs"
                                {:error error
                                 :event :link-preview/unfurl-urls}))}]}))

(defn- urls->previews
  [preview-cache urls]
  (->> urls
       (map #(get preview-cache % {:url % :loading? true}))
       (remove :failed?)))

(def new-request-id (comp str random-uuid))

(rf/defn unfurl-parsed-urls
  {:events [:link-preview/unfurl-parsed-urls]}
  [{:keys [db]} urls]
  (let [cleared (set (get-in db [:chat/link-previews :cleared]))]
    (when (or (empty? urls)
              (not= (set urls) cleared))
      (let [cache      (get-in db [:chat/link-previews :cache])
            previews   (urls->previews cache urls)
            new-urls   (->> previews
                            (filter :loading?)
                            (map :url))
            ;; `request-id` is a must because we need to process only the last
            ;; unfurling event, as well as avoid needlessly updating the app db
            ;; if the user changes the URLs in the input text when there are
            ;; in-flight RPC requests.
            request-id (new-request-id)]
        (merge {:db (-> db
                        (assoc-in [:chat/link-previews :unfurled] previews)
                        (assoc-in [:chat/link-previews :request-id] request-id)
                        (update :chat/link-previews dissoc :cleared))}
               (when (seq new-urls)
                 (log/debug "Unfurling URLs" {:urls new-urls :request-id request-id})
                 {:json-rpc/call
                  [{:method     "wakuext_unfurlURLs"
                    :params     [new-urls]
                    :on-success #(rf/dispatch [:link-preview/unfurl-parsed-urls-success request-id %])
                    :on-error   #(rf/dispatch [:link-preview/unfurl-parsed-urls-error request-id
                                               %])}]}))))))

(defn- failed-previews
  [curr-previews new-previews]
  (let [curr-urls (set (->> curr-previews
                            (filter :loading?)
                            (map :url)))
        new-urls  (set (map :url new-previews))]
    (map (fn [url]
           {:url url :failed? true})
         (set/difference curr-urls new-urls))))

(defn- reconcile-unfurled
  [curr-previews indexed-new-previews]
  (reduce (fn [acc preview]
            (if (:loading? preview)
              (if-let [loaded-preview (get indexed-new-previews
                                           (:url preview))]
                (conj acc loaded-preview)
                acc)
              (conj acc preview)))
          []
          curr-previews))

(rf/defn unfurl-parsed-urls-success
  {:events [:link-preview/unfurl-parsed-urls-success]}
  [{:keys [db]} request-id {new-previews :linkPreviews}]
  (when (= request-id (get-in db [:chat/link-previews :request-id]))
    (let [new-previews         (map data-store.messages/<-link-preview-rpc new-previews)
          curr-previews        (get-in db [:chat/link-previews :unfurled])
          indexed-new-previews (utils.collection/index-by :url new-previews)]
      (log/debug "URLs unfurled"
                 {:event      :link-preview/unfurl-parsed-urls-success
                  :previews   (map #(update % :thumbnail dissoc :data-uri) new-previews)
                  :request-id request-id})
      {:db (-> db
               (update-in [:chat/link-previews :unfurled] reconcile-unfurled indexed-new-previews)
               (update-in [:chat/link-previews :cache]
                          merge
                          indexed-new-previews
                          (utils.collection/index-by :url
                                                     (failed-previews curr-previews new-previews))))})))

(rf/defn unfurl-parsed-urls-error
  {:events [:link-preview/unfurl-parsed-urls-error]}
  [{:keys [db]} request-id error]
  (log/error "Failed to unfurl URLs"
             {:request-id request-id
              :error      error
              :event      :link-preview/unfurl-parsed-urls}))

(rf/defn reset-unfurled
  "Reset preview state, but keep the cache. Use this event after a message is
  sent."
  {:events [:link-preview/reset-unfurled]}
  [{:keys [db]}]
  {:db (update db :chat/link-previews dissoc :unfurled :request-id :cleared)})

(rf/defn reset-all
  "Reset all preview state. It is especially important to delete any cached
  URLs, as failing to do so results in its unbounded growth."
  {:events [:link-preview/reset-all]}
  [{:keys [db]}]
  {:db (dissoc db :chat/link-previews)})

(rf/defn clear-link-previews
  "Mark current unfurled URLs as `cleared`, meaning the user won't see previews
  until they insert/remove non-cleared URL(s)."
  {:events [:link-preview/clear]}
  [{:keys [db]}]
  (let [unfurled-urls (set (map :url (get-in db [:chat/link-previews :unfurled])))]
    {:db (-> db
             (update :chat/link-previews dissoc :unfurled :request-id)
             (assoc-in [:chat/link-previews :cleared] unfurled-urls))}))
