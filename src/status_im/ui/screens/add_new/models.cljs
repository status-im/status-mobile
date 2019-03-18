(ns status-im.ui.screens.add-new.models
  (:require [cljs.spec.alpha :as spec]
            [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.universal-links.core :as universal-links]
            [status-im.utils.fx :as fx]
            [status-im.browser.core :as browser]
            [status-im.constants :as constants]
            [status-im.extensions.core :as extensions]))

(defn- valid-url? [url]
  (boolean (re-matches constants/regx-url url)))

(fx/defn handle-qr-code
  [cofx data]
  (if (spec/valid? :global/public-key data)
    (universal-links/handle-view-profile cofx data)
    (or (universal-links/handle-url cofx data)
        (if (valid-url? data)
          (browser/handle-message-link cofx data)
          {:utils/show-popup {:title   (i18n/label :t/unable-to-read-this-code)
                              :content (i18n/label :t/use-valid-qr-code {:data data})
                              :on-dismiss #(re-frame/dispatch [:qr-scanner.ui/qr-code-error-dismissed])}}))))
