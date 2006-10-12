package org.seasar.cms.beantable;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


/**
 * <p><b>同期化：</b>
 * このクラスはスレッドセーフではありません。
 * </p>
 * 
 * @author YOKOTA Takehiko
 */
public class Formula
    implements Cloneable
{
    private String      base_;
    private Object[]    params_;
    private boolean[]   setParams_;

    private boolean     freeze_;


    /*
     * constructors
     */

    public Formula(String base)
    {
        base_ = base.trim();
        
        int pre = 0;
        int idx;
        int cnt = 0;
        while ((idx = base_.indexOf("?", pre)) >= 0) {
            cnt++;
            pre = idx + 1;
        }

        params_ = new Object[cnt];
        setParams_ = new boolean[cnt];
    }


    /*
     * static methods
     */

    public static String quoteString(String literal)
    {
        if (literal == null) {
            return null;
        }

        StringTokenizer st = new StringTokenizer(literal, "'", true);
        StringBuffer result = new StringBuffer("'");
        while (st.hasMoreTokens()) {
            String tkn = st.nextToken();
            if (tkn.equals("'")) {
                result.append("''");
            } else {
                result.append(tkn);
            }
        }
        result.append("'");

        return result.toString();
    }


    public static Formula newInstance(String formulaString)
    {
        if (formulaString == null) {
            return null;
        }

        List<String> list = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(formulaString, "'", true);
        StringBuffer sb = new StringBuffer();
        boolean inQuote = false;
        int quoteCount = 0;
        StringBuffer string = null;
        while (st.hasMoreTokens()) {
            String tkn = st.nextToken();
            if (!inQuote) {
                if (tkn.equals("'")) {
                    sb.append("?");
                    inQuote = true;
                    quoteCount = 0;
                    string = new StringBuffer();
                } else {
                    sb.append(tkn);
                }
            } else if (quoteCount == 0) {
                if (tkn.equals("'")) {
                    quoteCount++;
                } else {
                    string.append(tkn);
                }
            } else {
                if (tkn.equals("'")) {
                    string.append("'");
                    quoteCount = 0;
                } else {
                    list.add(string.toString());
                    inQuote = false;
                    sb.append(tkn);
                }
            }
        }
        if (inQuote) {
            if (quoteCount == 0) {
                throw new IllegalArgumentException(
                    "Syntax error: " + formulaString);
            }
            list.add(string.toString());
        }

        Formula formula = new Formula(sb.toString());
        int size = list.size();
        int idx = 1;
        for (int i = 0; i < size; i++) {
            formula.setString(idx++, list.get(i));
        }

        return formula;
    }


    public static Formula append(Formula o1, Formula o2)
    {
        return append(new Formula[]{ o1, o2 });
    }


    public static Formula append(Formula[] formulas)
    {
        List<Formula> list = new ArrayList<Formula>(formulas.length);
        int paramsCount = 0;
        for (int i = 0; i < formulas.length; i++) {
            if ((formulas[i] != null) && (formulas[i].length() > 0)) {
                list.add(formulas[i]);
                paramsCount += formulas[i].getParametersCount();
            }
        }
        int n = list.size();
        if (n == 0) {
            return new Formula("");
        } else if (n == 1) {
            return list.get(0);
        } else {
            StringBuffer sb = new StringBuffer();
            Object[] params = new Object[paramsCount];
            int idx = 0;
            for (int i = 0; i < n; i++) {
                Formula formula = list.get(i);
                sb.append(formula.getBase());
                int cnt = formula.getParametersCount();
                for (int j = 1; j <= cnt; j++) {
                    params[idx++] = formula.getParameter(j);
                }
            }
            Formula formula = new Formula(sb.toString());
            idx = 0;
            for (int i = 1; i <= paramsCount; i++) {
                formula.setObject(i, params[idx++]);
            }
            return formula;
        }
    }


    public static Formula intersection(Formula o1, Formula o2)
    {
        return intersection(new Formula[]{ o1, o2 });
    }


    public static Formula intersection(Formula[] formulas)
    {
        List<Formula> list = new ArrayList<Formula>(formulas.length);
        int paramsCount = 0;
        for (int i = 0; i < formulas.length; i++) {
            if ((formulas[i] != null) && (formulas[i].length() > 0)) {
                list.add(formulas[i]);
                paramsCount += formulas[i].getParametersCount();
            }
        }
        int n = list.size();
        if (n == 0) {
            return new Formula("");
        } else if (n == 1) {
            return (Formula)list.get(0).clone();
        } else {
            StringBuffer sb = new StringBuffer();
            Object[] params = new Object[paramsCount];
            int idx = 0;
            for (int i = 0; i < n; i++) {
                Formula formula = list.get(i);
                if (i > 0) {
                    sb.append(" AND ");
                }
                sb.append("(");
                sb.append(formula.getBase());
                sb.append(")");
                int cnt = formula.getParametersCount();
                for (int j = 1; j <= cnt; j++) {
                    params[idx++] = formula.getParameter(j);
                }
            }
            Formula formula = new Formula(sb.toString());
            idx = 0;
            for (int i = 1; i <= paramsCount; i++) {
                formula.setObject(i, params[idx++]);
            }
            return formula;
        }
    }


    /*
     * public scope methods
     */

    @Override
    public Object clone()
    {
        try {
            Formula formula = (Formula)super.clone();

            if (params_ != null) {
                Object[] params = new Object[params_.length];
                for (int i = 0; i < params_.length; i++) {
                    params[i] = params_[i];
                }
                formula.params_ = params;
            }

            if (setParams_ != null) {
                boolean[] setParams = new boolean[setParams_.length];
                for (int i = 0; i < setParams_.length; i++) {
                    setParams[i] = setParams_[i];
                }
                formula.setParams_ = setParams;
            }

            formula.freeze_ = false;

            return formula;
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException(ex);
        }
    }


    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }

        if ((obj != null) && (obj.getClass() == getClass())) {
            Formula formula = (Formula)obj;
            if (base_ == null) {
                if (formula.base_ != null) {
                    return false;
                }
            } else if (!base_.equals(formula.base_)) {
                return false;
            }

            if (params_ == null) {
                if (formula.params_ != null) {
                    return false;
                }
            } else if ((formula.params_ == null)
            || (params_.length != formula.params_.length)) {
                return false;
            } else {
                for (int i = 0; i < params_.length; i++) {
                    if (params_[i] == null) {
                        if (formula.params_[i] != null) {
                            return false;
                        }
                    } else if (!params_[i].equals(formula.params_[i])) {
                        return false;
                    }
                }
            }

            if (setParams_ == null) {
                if (formula.setParams_ != null) {
                    return false;
                }
            } else if ((formula.setParams_ == null)
            || (setParams_.length != formula.setParams_.length)) {
                return false;
            } else {
                for (int i = 0; i < setParams_.length; i++) {
                    if (setParams_[i] != formula.setParams_[i]) {
                        return false;
                    }
                }
            }

            return true;
        }

        return false;
    }


    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        int pre = 0;
        int ptr = 0;
        int idx = 0;
        while ((ptr = base_.indexOf("?", pre)) >= 0) {
            if (pre < ptr) {
                sb.append(base_.substring(pre, ptr));
            }
            if ((idx >= setParams_.length) || !setParams_[idx]) {
                // エラーであることを埋め込む。
                sb.append("****UNSET PARAMETER****");
            } else {
                if (params_[idx] == null) {
                    sb.append("NULL");
                } else if (params_[idx] instanceof String) {
                    sb.append(quoteString((String)params_[idx]));
                } else {
                    sb.append(params_[idx]);
                }
            }
            idx++;
            pre = ptr + 1;
        }
        if (pre < base_.length()) {
            sb.append(base_.substring(pre));
        }

        return sb.toString();
    }


    public int length()
    {
        return base_.length();
    }


    public String getBase()
    {
        return base_;
    }


    public int getParametersCount()
    {
        return params_.length;
    }


    public Object[] getParameters()
    {
        return params_;
    }


    public Object getParameter(int idx)
    {
        if ((idx < 1) || (idx > params_.length)) {
            throw new IllegalArgumentException("Index out of range: "
                + idx + " (" + params_.length + ")");
        }

        return params_[idx - 1];
    }


    public boolean validateParameters()
    {
        for (int i = 0; i < params_.length; i++) {
            if (!setParams_[i]) {
                return false;
            }
        }

        return true;
    }


    public Formula setInt(int idx, int value)
    {
        return setObject(idx, new Integer(value));
    }


    public Formula setString(int idx, String value)
    {
        return setObject(idx, value);
    }


    public Formula setDouble(int idx, double value)
    {
        return setObject(idx, new Double(value));
    }


    public Formula setNull(int idx)
    {
        return setObject(idx, null);
    }


    public Formula setObject(int idx, Object value)
    {
        if (freeze_) {
            throw new IllegalStateException("This object is freezed");
        }
        if ((idx < 1) || (idx > params_.length)) {
            throw new IllegalArgumentException("Index out of range: "
                + idx + " (" + params_.length + ")");
        }

        int i = idx - 1;
        params_[i] = value;
        setParams_[i] = true;

        return this;
    }


    public Formula freeze()
    {
        freeze_ = true;

        return this;
    }


    public boolean isFreezed()
    {
        return freeze_;
    }
}
