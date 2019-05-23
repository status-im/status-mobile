(ns status-im.extensions.capacities.map
  (:require-macros
   [status-im.utils.slurp :refer [slurp]]
   [status-im.utils.views :refer [defview letsubs]])
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.utils.platform :as platform]
            [status-im.utils.types :as types]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.browser.styles :as styles]
            [status-im.react-native.js-dependencies :as js-dependencies]))

(def mapview-html (slurp "resources/mapview/mapview.html"))

(def webview-class
  (memoize
   (fn []
     (reagent/adapt-react-class (.-WebView js-dependencies/webview)))))

(defn map-component
  "creates a webview reagent component which cause webview to be updated only when style changes.
  if injected-java-script changes, new javascript is run inside webview.
  so, injected-java-script is not only used on webview initialization, but also to update webview state"
  [opts wvref]
  (reagent.core/create-class
   {:should-component-update
    (fn [_ [_ {:keys [style injected-java-script]}] [_ {new-injected-java-script :injected-java-script new-style :style}]]
      (if (not= style new-style)
        true
        (do
          (when (and @wvref (not= injected-java-script new-injected-java-script))
            (.injectJavaScript ^js @wvref new-injected-java-script))
          false)))
    :reagent-render
    (fn [opts]
      [(webview-class) opts])}))

(defn- on-map-message [map-event on-change]
  (let [data (-> ^js map-event
                 (.-nativeEvent)
                 (.-data)
                 (types/json->clj))]
    (re-frame/dispatch (on-change {:value data}))))

(defn- web-view-error [_ code desc]
  (reagent/as-element
   [react/view styles/web-view-error
    [react/i18n-text {:style styles/web-view-error-text :key :web-view-error}]
    [react/text {:style styles/web-view-error-text}
     (str code)]
    [react/text {:style styles/web-view-error-text}
     (str desc)]]))

(defview map-webview [{:keys [interactive fly style marker on-change]}]
  (letsubs [webview (atom nil)]
    [map-component
     {:style                                 style
      :origin-whitelist                      ["*"]
      :source                                {:html mapview-html :base-url (cond
                                                                             platform/ios? "./mapview/"
                                                                             platform/android? "file:///android_asset/"
                                                                             :else nil)}
      :java-script-enabled                   true
      :bounces                               false
      :over-scroll-mode                      "never"
      :local-storage-enabled                 true
      :render-error                          web-view-error

      ;; load only local resources, for non-local resources open external browser
      :on-should-start-load-with-request     #(let [url (.-url %)]
                                                (if (string/starts-with? url "file")
                                                  true
                                                  (do (.openURL ^js (react/linking) url) false)))

      :ref                                   #(reset! webview %)
      :on-message                            #(when on-change (on-map-message % on-change))
      :injected-java-script                   (let [interactive? (boolean interactive) ;; force boolean
                                                    fly? (boolean fly)]
                                                (if (map? marker)
                                                  (str "update(" (types/clj->json (assoc marker :fly fly? :interactive interactive?)) ");")
                                                  (str "init(" (types/clj->json {:interactive interactive?}) ");")))}
     webview]))
