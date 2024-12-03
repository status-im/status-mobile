(ns status-im.contexts.settings.about.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn- navigate-back [] (rf/dispatch [:navigate-back]))

(defn- pending-link []
  (js/alert "Link unset"))

(defn- copy [data]
  (rf/dispatch [:share/copy-text-and-show-toast
                {:text-to-copy      data
                 :post-copy-message "Data copied"}]))

(def items
  [{:app-data? true}
   {:category-label "Website"
    :items          [{:title        "status.app"
                      :on-press     pending-link
                      :blur?        true
                      :action       :arrow
                      :action-props {:icon :i/external}}]}

   {:category-label "Github repos"
    :items          [{:title        "status-mobile"
                      :on-press     pending-link
                      :blur?        true
                      :action       :arrow
                      :action-props {:icon :i/external}}
                     {:title        "status-go"
                      :on-press     pending-link
                      :blur?        true
                      :action       :arrow
                      :action-props {:icon :i/external}}
                     {:title        "go-waku"
                      :on-press     pending-link
                      :blur?        true
                      :action       :arrow
                      :action-props {:icon :i/external}}]}

   ;; TODO: handle navigation for docs
   {:category-label "Documents"
    :items          [{:title    "Privacy policy"
                      :on-press #(rf/dispatch [:open-modal :screen/settings.privacy-policy])
                      :blur?    true
                      :action   :arrow}
                     {:title    "Terms of use"
                      :on-press #(rf/dispatch [:open-modal :screen/settings.terms-of-use])
                      :blur?    true
                      :action   :arrow}]}])

(defn category [{:keys [app-data? category-label items]}]
  (if app-data?
    (let [app-version  (rf/sub [:get-app-short-version])
          commit-hash  (rf/sub [:get-commit-hash])
          node-version (rf/sub [:get-app-node-version])]
      [rn/view {:style {:padding-horizontal 20
                        :padding-top        8
                        :padding-bottom     16
                        :row-gap            16}}
       [quo/data-item {:size          :default
                       :status        :default
                       :right-icon    :i/copy
                       :card?         true
                       :blur?         true
                       :title         "App version"
                       :on-press      #(copy app-version)
                       :subtitle-type :default
                       :subtitle      app-version}]
       [quo/data-item {:size          :default
                       :status        :default
                       :right-icon    :i/copy
                       :card?         true
                       :blur?         true
                       :title         "App commit"
                       :on-press      #(copy commit-hash)
                       :subtitle-type :default
                       :subtitle      commit-hash}]
       [quo/data-item {:size          :default
                       :status        :default
                       :right-icon    :i/copy
                       :card?         true
                       :blur?         true
                       :title         "Node version"
                       :on-press      #(copy node-version)
                       :subtitle-type :default
                       :subtitle      node-version}]])
    [quo/category
     {:label           category-label
      :list-type       :settings
      :container-style {:padding-bottom 12}
      :blur?           true
      :data            items}]))

(defn view []
  (let []
    [quo/overlay {:type :shell :top-inset? true}
     [quo/page-nav
      {:background :blur
       :icon-name  :i/arrow-left
       :on-press   navigate-back}]
     [quo/page-top {:title "About"}]
     [rn/flat-list
      {:data                            items
       :shows-vertical-scroll-indicator false
       :render-fn                       category
       :bounces                         false
       :over-scroll-mode                :never}]]))
