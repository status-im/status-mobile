(ns status-im.ui.components.svgimage
  (:require [status-im.ui.components.react :as react]
            [reagent.core :as reagent]
            [status-im.utils.platform :as platform]
            [status-im.utils.http :as http]))

(defn html [uri width height]
  (str
   "<!DOCTYPE html>\n
   <html>
   <head>
   <style type=\"text/css\">
   img {
        display: block;
        max-width:" width "px;
        max-height:" height "px;
        width: auto;
        height: auto;
        margin: auto;
    }
    div {
      width:" width "px;
      height:" height "px;
      display:table-cell;
      vertical-align:middle;
      text-align:center;
    }
    body {margin: 0;}

    </style>
    </head>
    <body>
    <div>
    <img src=" uri " align=\"middle\" />
    </div>
    </body>
    </html>"))

(defn svgimage [{:keys [style source]}]
  (let [width (reagent/atom nil)
        {:keys [uri k] :or {k 1}} source]
    (when (http/url-sanitized? uri)
      (fn []
        [react/view {:style     style
                     :on-layout #(reset! width (-> % .-nativeEvent .-layout .-width))}
         [react/web-view
          {:java-script-enabled         false
           :third-party-cookies-enabled false
           :scroll-enabled              false
           :bounces                     false
           :scales-page-to-fit          platform/android?
           :style                       {:width @width :height (* @width k) :background-color :transparent}
           :origin-whitelist             #js ["*"]
           :source                      {:html (html uri @width (* @width k))}}]]))))