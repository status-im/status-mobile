(ns status-im.ui.screens.desktop.main.buidl.core
  (:require [re-frame.core :as re-frame]
            [status-im.utils.handlers :as handlers]
            [status-im.chat.models.message :as message]
            [status-im.utils.fx :as fx]
            [cljs.spec.alpha :as spec]
            [status-im.utils.random :as random]
            [clojure.string :as string]
            [status-im.chat.models :as chat.models]
            [status-im.utils.build :as build]
            [status-im.ui.screens.navigation :as navigation]))

(spec/def :buidl/tag (spec/and :global/not-empty-string
                               (partial re-matches #"[a-z0-9\-]+")))
(spec/def :buidl/tags (spec/coll-of :buidl/tags :kind set?))

(spec/def ::content string?)
(spec/def ::title string?)
(spec/def ::tag-filter :buidl/tag)

(spec/def ::new-issue (spec/keys :opt-un [::content ::title :buidl/tags :buidl/tag :buidl/step]))
(spec/def ui/buidl (spec/keys :opt-un [::new-issue ::tag-filter]))

(re-frame/reg-sub
 :buidl/get-todos
 (fn [db]
   0))

(re-frame/reg-sub
 :buidl/get-messages
 (fn [db]
   (vals (get-in db [:chats "status-buidl-test" :messages]))))

(re-frame/reg-sub
 :buidl/get-tag-filter
 (fn [db]
   (get-in db [:ui/buidl :tag-filter] "")))

(re-frame/reg-sub
 :buidl/get-issues
 :<- [:buidl/get-messages]
 (fn [messages]
   (keep #(get-in % [:content :issue]) messages)))

(re-frame/reg-sub
 :buidl/get-filtered-issues
 :<- [:buidl/get-issues]
 :<- [:buidl/get-tag-filter]
 (fn [[issues tag-filter]]
   (if (empty? tag-filter)
     issues
     (keep #(when (some (fn [tag]
                          (clojure.string/includes? tag tag-filter))
                        (:tags %))
              %)
           issues))))

(re-frame/reg-sub
 :buidl/get-tags
 :<- [:buidl/get-issues]
 (fn [issues]
   (reduce (fn [acc {:keys [tags]}]
             (reduce (fn [acc tag]
                       (conj acc tag))
                     acc
                     tags))
           #{}
           issues)))

(re-frame/reg-sub
 :buidl/get-filtered-tags
 :<- [:buidl/get-tags]
 :<- [:buidl/get-tag-filter]
 (fn [[tags tag-filter]]
   (if (empty? tag-filter)
     tags
     (keep #(when (clojure.string/includes? % tag-filter)
              %)
           tags))))

(re-frame/reg-sub
 :buidl.ui/new-issue
 (fn [db]
   (get-in db [:ui/buidl :new-issue])))

(re-frame/reg-sub
 :buidl.new-issue.ui/tags
 :<- [:buidl.ui/new-issue]
 (fn [issue]
   (or (:tags issue)
       #{})))

(re-frame/reg-sub
 :buidl.new-issue.ui/tag-input
 :<- [:buidl.ui/new-issue]
 (fn [issue]
   (or (:tag issue)
       "")))

(re-frame/reg-sub
 :buidl.new-issue.ui/available-tags
 :<- [:buidl/get-tags]
 :<- [:buidl.new-issue.ui/tag-input]
 :<- [:buidl.new-issue.ui/tags]
 (fn [[existing-tags tag-input issue-tags]]
   (if (empty? tag-input)
     existing-tags
     (into #{} (filter #(and (string/starts-with? % tag-input)
                             (not (issue-tags %)))
                       (conj existing-tags tag-input))))))

(fx/defn send-buidl [cofx {:keys [issue] :as content}]
  (fx/merge cofx
            (chat.models/start-public-chat-without-navigation (str "status-buidl-test-tag-" (first (:tags issue))))
            (message/send-message {:chat-id      (str "status-buidl-test-tag-" (first (:tags issue)))
                                   :content-type "text/plain"
                                   :content (assoc content :text "You see this message because you are running a version of status that doesn't support BUIDL mode! Ask around on the #status-buidl channel how you can join the fork !")})
            (message/send-message {:chat-id      "status-buidl-test"
                                   :content-type "text/plain"
                                   :content (assoc content :text "You see this message because you are running a version of status that doesn't support BUIDL mode! Ask around on the #status-buidl channel how you can join the fork !")})))

(handlers/register-handler-fx
 :send-buidl-message
 (fn [cofx [_ buidl-message]]
   (send-buidl cofx buidl-message)))

(handlers/register-handler-fx
 :buidl/set-issue-input-field
 (fn [cofx [_ field text]]
   {:db (assoc-in (:db cofx) [:ui/buidl :new-issue field] text)}))

(handlers/register-handler-fx
 :buidl/tag-filter-changed
 (fn [cofx [_ tag-filter]]
   {:db (assoc-in (:db cofx) [:ui/buidl :tag-filter] tag-filter)}))

(handlers/register-handler-fx
 :buidl/add-tag
 (fn [cofx [_ tag]]
   (when (spec/valid? :buidl/tag tag)
     {:db (update-in (:db cofx) [:ui/buidl :new-issue :tags] #(if % (conj % tag) #{tag}))})))

(handlers/register-handler-fx
 :buidl/new-issue
 (fn [cofx _]
   (fx/merge cofx
             {:db (assoc-in (:db cofx) [:ui/buidl :new-issue] {:title "Title"
                                                               :content "Content"
                                                               :step :title})}
             (navigation/navigate-to-cofx :new-issue {}))))

(handlers/register-handler-fx
 :buidl/next-step
 (fn [cofx _]
   (let [step (get-in cofx [:db :ui/buidl :new-issue :step])]
     {:db (assoc-in (:db cofx) [:ui/buidl :new-issue :step] (case step
                                                              :title :content
                                                              :content :tags))})))

(handlers/register-handler-fx
 :buidl/create-issue
 (fn [cofx _]
   (let [{:keys [content tags title]} (get-in cofx [:db :ui/buidl :new-issue])]
     (fx/merge cofx
               {:db (-> (:db cofx)
                        (update :ui/buidl dissoc :new-issue)
                        (assoc :view-id :buidl))}
               (send-buidl {:issue {:tags tags
                                    :id (random/id)
                                    :title title
                                    :content content}})))))

#_(spec/def :buidl/issue (spec/keys :req-un [::id]
                                    :opt-un [::tags ::title ::content ::comment]))

#_(spec/def :buidl/user-tags (spec/keys :req-un [::add ::remove]))
