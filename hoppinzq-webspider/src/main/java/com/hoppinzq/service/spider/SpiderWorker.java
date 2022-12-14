package com.hoppinzq.service.spider;

import com.hoppinzq.service.html.*;
import com.hoppinzq.service.log.Log;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author: zq
 */
public class SpiderWorker extends Thread {
    protected String _target;
    protected Spider _owner;
    protected boolean _busy;
    protected HTTP _http;

    public SpiderWorker(Spider spider, HTTP http) {
        this._http = http;
        this._owner = spider;
    }

    public boolean isBusy() {
        return this._busy;
    }

    public void run() {
        while(true) {
            this._target = this._owner.getWorkload();
            if (this._target == null) {
                return;
            }

            this._owner.getSpiderDone().workerBegin();
            this.processWorkload();
            this._owner.getSpiderDone().workerEnd();
        }
    }

    protected void processWorkload() {
        try {
            this._busy = true;
            Log.log(3, "爬取ing " + this._target);
            this._http.send(this._target, (String)null);
            HTMLParser htmlParser = new HTMLParser();
            htmlParser._source = new StringBuffer(this._http.getBody());
            this._owner.processPage(this._http);

            while(true) {
                Attribute attribute;
                do {
                    char c;
                    do {
                        if (htmlParser.eof()) {
                            return;
                        }

                        c = htmlParser.get();
                    } while(c != 0);

                    HTMLTag htmlTag = htmlParser.getTag();
                    attribute = htmlTag.get("HREF");
                } while(attribute == null);

                URL url = null;

                try {
                    url = new URL(new URL(this._target), attribute.getValue());
                } catch (MalformedURLException malformedURLException) {
                    Log.log(2, "Spider找到其他链接: " + attribute);
                    this._owner.foundOtherLink(attribute.getValue());
                    continue;
                }

                if (this._owner.getRemoveQuery()) {
                    url = URLUtility.stripQuery(url);
                }

                url = URLUtility.stripAnhcor(url);
                if (url.getHost().equalsIgnoreCase((new URL(this._target)).getHost())) {
                    Log.log(3, "Spider找到内部链接: " + url.toString());
                    this._owner.foundInternalLink(url.toString());
                } else {
                    Log.log(3, "Spider找到外部链接: " + url.toString());
                    this._owner.foundExternalLink(url.toString());
                }

                this._owner.completePage(this._http, false);
            }
        } catch (IOException ioException) {
            Log.log(4, "加载文件时出错(" + this._target + "): " + ioException);
        } catch (Exception exception) {
            Log.logException("处理文件时出现异常(" + this._target + "): ", exception);
        } finally {
            this._owner.completePage(this._http, true);
            this._busy = false;
        }

    }

    public HTTP getHTTP() {
        return this._http;
    }
}
