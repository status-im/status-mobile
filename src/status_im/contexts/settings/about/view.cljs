(ns status-im.contexts.settings.about.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.constants :as constants]
            [status-im.contexts.settings.about.style :as style]
            [status-im.contexts.settings.common.header :as header]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn- open-link
  [url]
  (rf/dispatch [:browser.ui/open-url url]))

(defn- copy
  [data item-name]
  (rf/dispatch [:share/copy-text-and-show-toast
                {:text-to-copy      data
                 :post-copy-message (str item-name
                                         " "
                                         (i18n/label :t/sharing-copied-to-clipboard))}]))

(def about-data
  [{:app-info? true}
   {:category-label (i18n/label :t/website)
    :items          [{:title        "status.app"
                      :on-press     #(open-link constants/status-app-url)
                      :blur?        true
                      :action       :arrow
                      :action-props {:icon :i/external}}]}
   {:category-label (i18n/label :t/github-repos)
    :items          [{:title        "status-mobile"
                      :on-press     #(open-link constants/status-mobile-url)
                      :blur?        true
                      :action       :arrow
                      :action-props {:icon :i/external}}
                     {:title        "status-go"
                      :on-press     #(open-link constants/status-go-url)
                      :blur?        true
                      :action       :arrow
                      :action-props {:icon :i/external}}
                     {:title        "go-waku"
                      :on-press     #(open-link constants/go-waku-url)
                      :blur?        true
                      :action       :arrow
                      :action-props {:icon :i/external}}]}
   {:category-label (i18n/label :t/documents)
    :items          [{:title    (i18n/label :t/privacy-policy)
                      :on-press #(rf/dispatch [:open-modal :screen/settings.privacy-policy])
                      :blur?    true
                      :action   :arrow}
                     {:title    (i18n/label :t/terms-of-service)
                      :on-press #(rf/dispatch [:open-modal :screen/settings.terms-of-use])
                      :blur?    true
                      :action   :arrow}]}])

(defn info-item
  [{:keys [title info]}]
  [quo/data-item
   {:size                :default
    :status              :default
    :right-icon          :i/copy
    :card?               true
    :blur?               true
    :title               title
    :on-press            #(copy info title)
    :subtitle            info
    :subtitle-type       :default
    :subtitle-text-props {:number-of-lines 1
                          :ellipsize-mode  :middle}}])

(defn- app-info
  []
  (let [app-version  (rf/sub [:get-app-short-version])
        commit-hash  (rf/sub [:get-commit-hash])
        node-version (rf/sub [:get-app-node-version])]
    [rn/view {:style style/app-info-container}
     [info-item {:title (i18n/label :t/version) :info app-version}]
     [info-item {:title (i18n/label :t/app-commit) :info commit-hash}]
     [info-item {:title (i18n/label :t/node-version) :info node-version}]]))

(defn category
  [{:keys [app-info? category-label items]}]
  (if app-info?
    [app-info]
    [quo/category
     {:label           category-label
      :list-type       :settings
      :container-style style/category-spacing
      :blur?           true
      :data            items}]))

(defn view
  []
  [quo/overlay {:type :shell}
   [header/view {:title (i18n/label :t/about)}]
   [rn/flat-list
    {:data                            about-data
     :shows-vertical-scroll-indicator false
     :render-fn                       category
     :bounces                         false
     :over-scroll-mode                :never}]])
