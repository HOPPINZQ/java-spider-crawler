package com.hoppinzq.service.spider;


import com.hoppinzq.service.log.Log;
import com.hoppinzq.service.spiderService.IWorkloadStorable;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * @author: zq
 */
public class SpiderInternalWorkload implements IWorkloadStorable {
    Hashtable _complete = new Hashtable();
    Vector _waiting = new Vector();
    Vector _running = new Vector();

    public SpiderInternalWorkload() {
    }

    public synchronized String assignWorkload() {
        if (this._waiting.size() < 1) {
            return null;
        } else {
            String element = (String)this._waiting.firstElement();
            if (element != null) {
                this._waiting.remove(element);
                this._running.addElement(element);
            }

            Log.log(2, "蜘蛛分配工作:" + element);
            return element;
        }
    }

    public synchronized void addWorkload(String url) {
        if (this.getURLStatus(url) == 'U') {
            this._waiting.addElement(url);
            Log.log(2, "蜘蛛添加工作:" + url);
        }
    }

    public synchronized void completeWorkload(String ele, boolean isE) {
        if (this._running.size() > 0) {
            Enumeration elements = this._running.elements();

            while(elements.hasMoreElements()) {
                String elem = (String)elements.nextElement();
                if (elem.equals(ele)) {
                    this._running.remove(elem);
                    if (isE) {
                        Log.log(2, "蜘蛛工作错误:" + ele);
                        this._complete.put(elem, "e");
                    } else {
                        Log.log(2, "蜘蛛工作完成:" + ele);
                        this._complete.put(elem, "c");
                    }

                    return;
                }
            }
        }

        Log.log(4, "蜘蛛工作丢失:" + ele);
    }

    public synchronized char getURLStatus(String url) {
        if (this._complete.get(url) != null) {
            return 'C';
        } else {
            Enumeration enumeration;
            String temp;
            if (this._waiting.size() > 0) {
                enumeration = this._waiting.elements();

                while(enumeration.hasMoreElements()) {
                    temp = (String)enumeration.nextElement();
                    if (temp.equals(url)) {
                        return 'W';
                    }
                }
            }

            if (this._running.size() > 0) {
                enumeration = this._running.elements();

                while(enumeration.hasMoreElements()) {
                    temp = (String)enumeration.nextElement();
                    if (temp.equals(url)) {
                        return 'R';
                    }
                }
            }

            return 'U';
        }
    }

    public synchronized void clear() {
        this._waiting.clear();
        this._complete.clear();
        this._running.clear();
    }

    @Override
    public String getWork() {
        return "等待的工作："+this._waiting.toString()
                +"\n"+"正在运行的工作："+this._running.toString()
                +"\n"+"已完成的工作："+this._complete.toString();
    }
}
