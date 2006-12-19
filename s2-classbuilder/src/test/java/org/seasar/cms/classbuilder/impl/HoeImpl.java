package org.seasar.cms.classbuilder.impl;

public class HoeImpl
    implements Hoe
{
    private String name_;

    private Fuga fuga_;


    public String getName()
    {
        return name_;
    }


    public void setName(String name)
    {
        name_ = name;
    }


    public Fuga getFuga()
    {
        return fuga_;
    }


    public void setFuga(Fuga fuga)
    {
        fuga_ = fuga;
    }
}
