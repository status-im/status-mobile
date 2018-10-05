(ns status-im.ui.screens.desktop.main.buidl.core
  (:require [re-frame.core :as re-frame]
            [status-im.utils.handlers :as handlers]
            [status-im.chat.models.message :as message]
            [status-im.utils.fx :as fx]
            [cljs.spec.alpha :as spec]
            [status-im.utils.random :as random]
            [clojure.string :as string]
            [status-im.chat.models :as chat.models]))

(spec/def :buidl/tag (spec/and :global/not-empty-string
                               (partial re-matches #"[a-z0-9\-]+")))
(spec/def :buidl/tags (spec/coll-of :buidl/tags :kind set?))

(spec/def ::content string?)
(spec/def ::title string?)

(spec/def ui/buidl (spec/keys :opt-un [::content ::title :buidl/tags :buidl/tag :buidl/step]))

(re-frame/reg-sub
 :buidl/get-messages
 (fn [db]
   (vals (get-in db [:chats "status-buidl-test" :messages]))))

(re-frame/reg-sub
 :buidl/get-issues
 :<- [:buidl/get-messages]
 (fn [messages]
   (keep #(get-in % [:content :issue]) messages)))

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
 :buidl.ui/issue
 (fn [db]
   (:ui/buidl db)))

(re-frame/reg-sub
 :buidl.issue.ui/tags
 :<- [:buidl.ui/issue]
 (fn [issue]
   (or (:tags issue)
       #{})))

(re-frame/reg-sub
 :buidl.issue.ui/tag-input
 :<- [:buidl.ui/issue]
 (fn [issue]
   (or (:tag issue)
       "")))

(re-frame/reg-sub
 :buidl.issue.ui/available-tags
 :<- [:buidl/get-tags]
 :<- [:buidl.issue.ui/tag-input]
 :<- [:buidl.issue.ui/tags]
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
   {:db (assoc-in (:db cofx) [:ui/buidl field] text)}))

(handlers/register-handler-fx
 :buidl/add-tag
 (fn [cofx [_ tag]]
   (when (spec/valid? :buidl/tag tag)
     {:db (update-in (:db cofx) [:ui/buidl :tags] #(if % (conj % tag) #{tag}))})))

(defn generate-tags [tags]
  (into #{} (keep #(when (spec/valid? :buidl/tag %) %)
                  (string/split tags #" "))))

(handlers/register-handler-fx
 :buidl/new-issue
 (fn [cofx _]
   {:db (assoc (:db cofx) :ui/buidl {:step :title})}))

(handlers/register-handler-fx
 :buidl/next-step
 (fn [cofx _]
   (let [step (get-in cofx [:db :ui/buidl :step])]
     {:db (assoc-in (:db cofx) [:ui/buidl :step] (case step
                                                   :title :content
                                                   :content :tags))})))

(handlers/register-handler-fx
 :buidl/create-issue
 (fn [cofx _]
   (let [{:keys [content tags title]} (get-in cofx [:db :ui/buidl])]
     (fx/merge cofx
               {:db (dissoc (:db cofx) :ui/buidl)}
               (send-buidl {:issue {:tags tags
                                    :id (random/id)
                                    :title title
                                    :content content}})))))

#_(spec/def :buidl/issue (spec/keys :req-un [::id]
                                    :opt-un [::tags ::title ::content ::comment]))

#_(spec/def :buidl/user-tags (spec/keys :req-un [::add ::remove]))
